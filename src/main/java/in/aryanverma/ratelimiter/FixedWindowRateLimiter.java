package in.aryanverma.ratelimiter;

import in.aryanverma.ratelimiter.exception.LimitMismatchException;
import in.aryanverma.ratelimiter.exception.LimitsEmptyException;
import in.aryanverma.ratelimiter.limit.FixedWindowLimit;
import in.aryanverma.ratelimiter.limit.Limit;
import in.aryanverma.ratelimiter.luascript.FixedWindowLuaScript;
import in.aryanverma.ratelimiter.luascript.LuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.Arrays;
import java.util.List;

public class FixedWindowRateLimiter extends RateLimiter{

    public FixedWindowRateLimiter(String rateLimiterId, JedisPool jedisPool) {
        super(rateLimiterId, jedisPool);
    }

    //for fixed window rate limiter, we return the fixed window lua script instance.
    protected LuaScript createLuaScript(Jedis jedis) {
        return new FixedWindowLuaScript(jedis);
    }

    @Override
    public boolean tryRequest(String identity, int cost) throws LimitsEmptyException {
        if(limits.isEmpty()) throw new LimitsEmptyException("Limit is empty");
        long timestamp = System.currentTimeMillis()/1000; // timestamp in seconds because granularity for fixed window is in seconds.
        try(Jedis jedis = this.jedisPool.getResource()){ // getting a connection from the jedis pool
            for(Limit limit: this.limits){
                long bucket = (timestamp/limit.getPeriod().getSeconds())*(limit.getPeriod().getSeconds()); // bucket (in seconds) the current time falls into
                String key = RateLimiterUtility.getKeyWithTimestamp(identity, this.rateLimiterId, limit.toString(), bucket); // key parameter for lua script in redis
                List<String> argv = Arrays.asList(limit.getCapacity().toString(), Long.toString(limit.getPeriod().getSeconds()), Integer.toString(cost)); // argv parameter for lua script in redis
                Object allow = jedis.evalsha(script.getSha(), Arrays.asList(key), argv); // using evalsha to run lua script. storing result in counter.
                if((Long)allow == 0) return false;
            }
        }
        return true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws LimitMismatchException{
        if(limit instanceof FixedWindowLimit) return; // if limit type is not FixedWindowLimit throw an error
        throw new LimitMismatchException("Limit type is not FixedWindowLimit");
    }

    @Override
    public String toString() {
        return "fixedWindow";
    }
}
