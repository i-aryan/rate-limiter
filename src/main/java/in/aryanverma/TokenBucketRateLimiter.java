package in.aryanverma;

import in.aryanverma.limit.Limit;
import in.aryanverma.limit.TokenBucketLimit;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.TokenBucketLuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.Arrays;
import java.util.List;

public class TokenBucketRateLimiter extends RateLimiter{

    public TokenBucketRateLimiter(String rateLimiterId, JedisPool jedisPool){
        super(rateLimiterId, jedisPool);
    }

    //returns token bucket lua script instance
    protected LuaScript createLuaScript(Jedis jedis) {
        return new TokenBucketLuaScript(jedis);
    }

    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException{
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){ // getting a connection from the jedis pool
            for(Limit limit: limits) {
                List<String> keys = Arrays.asList(RateLimiterUtility.getKey(identity, this.rateLimiterId, limit.toString()),
                        RateLimiterUtility.getTimestampKey(identity, this.rateLimiterId, limit.toString())); //keys parameter: key, timestamp key
                List<String> args = Arrays.asList(
                        Integer.toString(limit.getCapacity()),
                        Integer.toString(limit.getRate()),
                        Long.toString(timestamp),
                        Integer.toString(cost)
                ); //argv parameter for lua script: capacity, rate, current timestamp, cost
                Object response = jedis.evalsha(script.getSha(), keys, args); // calling evalsha by passing the SHA from script object
                if((Long)response == 0) return false;
            }
        }
        return true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof TokenBucketLimit) return; // throw error if limit type does not match token bucket limit
        throw new RateLimiterException("Limit type is not TokenBucketLimit");
    }

    @Override
    public String toString() {
        return "tokenBucket";
    }
}
