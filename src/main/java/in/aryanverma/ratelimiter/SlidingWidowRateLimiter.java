package in.aryanverma.ratelimiter;

import in.aryanverma.ratelimiter.exception.LimitMismatchException;
import in.aryanverma.ratelimiter.exception.LimitsEmptyException;
import in.aryanverma.ratelimiter.limit.Limit;
import in.aryanverma.ratelimiter.limit.SlidingWindowLimit;
import in.aryanverma.ratelimiter.luascript.LuaScript;
import in.aryanverma.ratelimiter.luascript.SlidingWindowLuaScript;
import redis.clients.jedis.*;
import java.util.Arrays;
import java.util.List;

public class SlidingWidowRateLimiter extends RateLimiter{

    public SlidingWidowRateLimiter(String rateLimiterId, JedisPool jedisPool){
        super(rateLimiterId, jedisPool);
    }

    // returns sliding window lua script
    protected LuaScript createLuaScript(Jedis jedis) {
        return new SlidingWindowLuaScript(jedis);
    }

    @Override
    public boolean tryRequest(String identity, int cost) throws LimitsEmptyException {
        if(limits.isEmpty()) throw new LimitsEmptyException("Limit is empty");
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){
            for(Limit limit: this.limits){
                String key = RateLimiterUtility.getKey(identity, this.rateLimiterId, limit.toString());
                List<String> keys = Arrays.asList(key); // keys parameter for lua script
                List<String> argv = Arrays.asList(limit.getCapacity().toString(), Long.toString(limit.getPeriod().getSeconds()*1000),
                        limit.getLookBackCount().toString(), Long.toString(timestamp),
                        Integer.toString(cost)); // argv parameter: capacity, time period in ms, lookback count, timestamp, cost
                Object response = jedis.evalsha(this.script.getSha(), keys, argv); // calling evalsha by passing the SHA from script object
                if((Long)response == 0) return false;
            }
        }
        return  true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws LimitMismatchException{
        if(limit instanceof SlidingWindowLimit) return; // throw error if limit type does not match sliding window limit
        throw new LimitMismatchException("Limit type is not SlidingWindowLimit");
    }
    @Override
    public String toString() {
        return "slidingWindow";
    }
}
