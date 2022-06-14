package in.aryanverma;

import in.aryanverma.limit.Limit;
import in.aryanverma.luascript.LeakyBucketLuaScript;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.TokenBucketLuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import javax.naming.spi.ObjectFactoryBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeakyBucketRateLimiter extends RateLimiter{

    private LuaScript script;
    public LeakyBucketRateLimiter(JedisPool jedisPool) {
        super(jedisPool);
        try(Jedis jedis = jedisPool.getResource()) {
            script = new LeakyBucketLuaScript(jedis);
        }
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterError {
        if(limits.isEmpty()) throw new RateLimiterError("Limit is empty");
        if(limits.size() > 1) throw new RateLimiterError("Cannot have more than 1 limit in Leaky Bucket Rate limiter");
        long timestamp = System.currentTimeMillis();
        Object response;
        try(Jedis jedis = jedisPool.getResource()){
            Limit limit = this.limits.get(0);
            String key = RateLimiterUtility.getKey(identity, this.toString(), limit.toString());
            String timestampKey = RateLimiterUtility.getTimestampKey(identity, this.toString(), limit.toString());
            List<String> keys = Arrays.asList(key, timestampKey);
            List<String> argv = Arrays.asList(limit.getCapacity().toString(), limit.getDispatchInterval().toString(), Long.toString(timestamp), RateLimiterUtility.getKeyWithRandomNumber(timestamp));
            response = jedis.evalsha(script.getSha(), keys, argv);
        }
//        System.out.println(((ArrayList<Long>)response).get(0));
        if (((ArrayList<Long>)response).get(0) == 0) {
            System.out.println(timestamp/1000 + ", false");
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

        System.out.println(timestamp/1000 + ", true, " + System.currentTimeMillis()/1000 + ", wait:" + ((ArrayList<Long>)response).get(1)/1000);
        return true;
    }

    @Override
    public String toString() {
        return "leakyBucket";
    }
}
