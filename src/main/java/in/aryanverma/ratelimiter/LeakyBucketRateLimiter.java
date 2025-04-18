package in.aryanverma.ratelimiter;

import in.aryanverma.ratelimiter.exception.LimitMismatchException;
import in.aryanverma.ratelimiter.exception.LimitsEmptyException;
import in.aryanverma.ratelimiter.exception.MultipleLimitLeakyBucketException;
import in.aryanverma.ratelimiter.limit.LeakyBucketLimit;
import in.aryanverma.ratelimiter.limit.Limit;
import in.aryanverma.ratelimiter.luascript.LeakyBucketLuaScript;
import in.aryanverma.ratelimiter.luascript.LuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeakyBucketRateLimiter extends RateLimiter{

    public LeakyBucketRateLimiter(String rateLimiterId, JedisPool jedisPool) {
        super(rateLimiterId, jedisPool);
    }

    // returns leaky bucket lua script instance
    protected LuaScript createLuaScript(Jedis jedis) {
        return new LeakyBucketLuaScript(jedis);
    }

    @Override
    public boolean tryRequest(String identity, int cost) throws LimitsEmptyException, MultipleLimitLeakyBucketException {
        if(limits.isEmpty()) throw new LimitsEmptyException("Limit is empty");
        if(limits.size() > 1) throw new MultipleLimitLeakyBucketException("Cannot have more than 1 limit in Leaky Bucket Rate limiter"); //throw error if more than 1 limit in leaky bucket

        long timestamp = System.currentTimeMillis();
        Object response;
        try(Jedis jedis = jedisPool.getResource()){
            Limit limit = this.limits.get(0);
            List<String> keys = Arrays.asList(RateLimiterUtility.getKey(identity, this.rateLimiterId, limit.toString())
                    ,RateLimiterUtility.getTimestampKey(identity, this.rateLimiterId, limit.toString())); //key for sorted set and for lastDispatch time
            List<String> argv = Arrays.asList(limit.getCapacity().toString(), limit.getRate().toString(), Long.toString(timestamp),
                    RateLimiterUtility.getKeyWithRandomNumber(timestamp), Integer.toString(cost)); // argv parameter: capacity, rate, current timestamp, sorted set element key, cost
            response = jedis.evalsha(script.getSha(), keys, argv);
        }

        if (((ArrayList<Long>)response).get(0) == 0) return false; //if not allowed, return false
        try {
            Thread.sleep(((ArrayList<Long>)response).get(1)); // otherwise, sleep for required milliseconds returned by the script
        }
        catch (InterruptedException e){
            System.out.println(e);
        }
        return true;
    }
    @Override
    protected void checkLimitType(Limit limit) throws LimitMismatchException{
        if(limit instanceof LeakyBucketLimit) return; //throw error if limit type does not match leakybucketlimit
        throw new LimitMismatchException("Limit type is not LeakyBucketLimit");
    }

    @Override
    public String toString() {
        return "leakyBucket";
    }
}
