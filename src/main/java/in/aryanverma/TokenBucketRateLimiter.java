package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.limit.TokenBucketLimit;
import in.aryanverma.luascript.FixedWindowLuaScript;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.TokenBucketLuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenBucketRateLimiter extends RateLimiter{


    public TokenBucketRateLimiter(String rateLimiterId, JedisPool jedisPool){
        super(rateLimiterId, jedisPool);
    }
    protected LuaScript createLuaScript(Jedis jedis) {
        return new TokenBucketLuaScript(jedis);
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException{
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){
            for(Limit limit: limits) {
                List<String> keys = Arrays.asList(RateLimiterUtility.getKey(identity, this.rateLimiterId, limit.toString()),
                        RateLimiterUtility.getTimestampKey(identity, this.rateLimiterId, limit.toString()));
                List<String> args = Arrays.asList(
                        Integer.toString(limit.getCapacity()),
                        Integer.toString(limit.getRate()),
                        Long.toString(timestamp),
                        Integer.toString(cost)
                );
                Object response = jedis.evalsha(script.getSha(), keys, args);
                if((Long)response == 0) {
//                    System.out.println(timestamp/1000 + ", false");
                    return false;
                }
            }
        }
//        System.out.println(timestamp/1000 + ", true");
        return true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof TokenBucketLimit) return;
        throw new RateLimiterException("Limit type is not TokenBucketLimit");
    }

    @Override
    public String toString() {
        return "tokenBucket";
    }
}
