package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.LeakyBucketLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.luascript.FixedWindowLuaScript;
import in.aryanverma.luascript.LeakyBucketLuaScript;
import in.aryanverma.luascript.LuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeakyBucketRateLimiter extends RateLimiter{

    public LeakyBucketRateLimiter(JedisPool jedisPool) {
        super(jedisPool);
    }

    protected LuaScript createLuaScript(Jedis jedis) {
        return new LeakyBucketLuaScript(jedis);
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException {
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        if(limits.size() > 1) throw new RateLimiterException("Cannot have more than 1 limit in Leaky Bucket Rate limiter");
        long timestamp = System.currentTimeMillis();
        Object response;
        try(Jedis jedis = jedisPool.getResource()){
            Limit limit = this.limits.get(0);
            String key = RateLimiterUtility.getKey(identity, this.toString(), limit.toString());
            String timestampKey = RateLimiterUtility.getTimestampKey(identity, this.toString(), limit.toString());
            List<String> keys = Arrays.asList(key, timestampKey);
            List<String> argv = Arrays.asList(limit.getCapacity().toString(), limit.getRate().toString(), Long.toString(timestamp), RateLimiterUtility.getKeyWithRandomNumber(timestamp));
            response = jedis.evalsha(script.getSha(), keys, argv);
        }
//        System.out.println(((ArrayList<Long>)response).get(0));
        if (((ArrayList<Long>)response).get(0) == 0) {
//            System.out.println(timestamp/1000 + ", false");
            return false;
        }
//        System.out.println(((ArrayList<Long>)response).get(1));
//        try {
//            Thread.sleep(((ArrayList<Long>)response).get(1));
//        }
//        catch (InterruptedException e){
//            System.out.println(e);
//        }
        while(((ArrayList<Long>)response).get(1)>System.currentTimeMillis());

        System.out.println(timestamp + ", true, " + System.currentTimeMillis() + ", wait:" + ((ArrayList<Long>)response).get(1));
        return true;
    }
    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof LeakyBucketLimit) return;
        throw new RateLimiterException("Limit type is not LeakyBucketLimit");
    }

    @Override
    public String toString() {
        return "leakyBucket";
    }
}
