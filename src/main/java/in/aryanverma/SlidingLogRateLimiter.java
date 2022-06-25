package in.aryanverma;

import in.aryanverma.limit.Limit;
import in.aryanverma.limit.SlidingLogLimit;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.SlidingLogLuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlidingLogRateLimiter extends RateLimiter{


    public SlidingLogRateLimiter(String rateLimiterId, JedisPool jedisPool){
        super(rateLimiterId, jedisPool);
    }

    // returns sliding log lua script
    protected LuaScript createLuaScript(Jedis jedis) {
        return new SlidingLogLuaScript(jedis);
    }

    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException{
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){ // gets a jedis connection from the pool
            List<String> keys = Arrays.asList(RateLimiterUtility.getKey(identity, this.rateLimiterId, "nill")); // keys parameter consists of key name
            List<String> argv = new ArrayList<>();
            argv.add(Long.toString(timestamp)); // adding timestamp to argv
            argv.add(RateLimiterUtility.getKeyWithRandomNumber(timestamp)); // key for sorted set element
            argv.add(Integer.toString(cost)); // adding cost
            for(Limit limit: this.limits){ // iterating through all limits and adding their capacity and time period to argv parameter
                argv.add(limit.getCapacity().toString());
                argv.add(Long.toString(limit.getPeriod().getSeconds()));
            }
            Object response = jedis.evalsha(this.script.getSha(), keys , argv); // calling evalsha by passing the SHA from script object
            if((Long)response==0) return false;
        }
        return true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof SlidingLogLimit) return; // throw error if limit type does not match
        throw new RateLimiterException("Limit type is not SlidingLogLimit");
    }

    @Override
    public String toString() {
        return "slidingLog";
    }
}
