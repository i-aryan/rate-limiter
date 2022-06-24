package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.limit.SlidingWindowLimit;
import in.aryanverma.luascript.FixedWindowLuaScript;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.SlidingWindowLuaScript;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlidingWidowRateLimiter extends RateLimiter{

    public SlidingWidowRateLimiter(String rateLimiterId, JedisPool jedisPool){
        super(rateLimiterId, jedisPool);
    }

    protected LuaScript createLuaScript(Jedis jedis) {
        return new SlidingWindowLuaScript(jedis);
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException{
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){
            int index = 0;
            for(Limit limit: this.limits){
                String key = RateLimiterUtility.getKey(identity, this.rateLimiterId, limit.toString());
                List<String> keys = Arrays.asList(key);
                List<String> argv = Arrays.asList(limit.getCapacity().toString(), Long.toString(limit.getPeriod().getSeconds()*1000), limit.getLookBackCount().toString(), Long.toString(timestamp), Integer.toString(cost));
                Object response= jedis.evalsha(this.script.getSha(), keys, argv);
                if((Long)response == 0) {
//                    System.out.println(timestamp/1000 + ", false");
                    return false;
                }
            }
        }
//        System.out.println(timestamp/1000 + ", true");
        return  true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof SlidingWindowLimit) return;
        throw new RateLimiterException("Limit type is not SlidingWindowLimit");
    }
    @Override
    public String toString() {
        return "slidingWindow";
    }
}
