package in.aryanverma;

import com.google.gson.internal.bind.util.ISO8601Utils;
import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.luascript.FixedWindowLuaScript;
import in.aryanverma.luascript.LeakyBucketLuaScript;
import in.aryanverma.luascript.LuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FixedWindowRateLimiter extends RateLimiter{

    public FixedWindowRateLimiter(String rateLimiterId, JedisPool jedisPool) {
        super(rateLimiterId, jedisPool);
    }

    protected LuaScript createLuaScript(Jedis jedis) {
        return new FixedWindowLuaScript(jedis);
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException {
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        long timestamp = System.currentTimeMillis()/1000;
//        long timestamp2 = System.currentTimeMillis();
        try(Jedis jedis = this.jedisPool.getResource()){
            for(Limit limit: this.limits){
                long bucket = (timestamp/limit.getPeriod().getSeconds())*(limit.getPeriod().getSeconds());
                String key = RateLimiterUtility.getKeyWithTimestamp(identity, this.rateLimiterId, limit.toString(), bucket);
                List<String> argv = Arrays.asList(limit.getCapacity().toString(), Long.toString(limit.getPeriod().getSeconds()), Integer.toString(cost));
                Object counter = jedis.evalsha(script.getSha(), Arrays.asList(key), argv);
                if((Long)counter == 0) {
//                    System.out.println(timestamp + ", false, " + System.currentTimeMillis());
                    return false;
                }
            }
        }
//        System.out.println(timestamp2 + ", true, " + System.currentTimeMillis());
        return true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof FixedWindowLimit) return;
        throw new RateLimiterException("Limit type is not FixedWindowLimit");
    }

    @Override
    public String toString() {
        return "fixedWindow";
    }
}
