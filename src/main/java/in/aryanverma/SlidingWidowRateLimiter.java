package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.limit.SlidingWindowLimit;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.SlidingWindowLuaScript;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlidingWidowRateLimiter extends RateLimiter{

    private LuaScript script;

    public SlidingWidowRateLimiter(JedisPool jedisPool){
        super(jedisPool);
        try(Jedis jedis = jedisPool.getResource()) {
            script = new SlidingWindowLuaScript(jedis);
        }
    }
    @Override
    public boolean tryRequest(String identity, int cost) {
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){
            int index = 0;
            for(Limit limit: this.limits){
                String key = RateLimiterUtility.getKey(identity, this.toString(), limit.toString());
                List<String> keys = Arrays.asList(key);
                List<String> argv = Arrays.asList(limit.getCapacity().toString(), Long.toString(limit.getPeriod().getSeconds()*1000), limit.getLookBackCount().toString(), Long.toString(timestamp));
                Object response= jedis.evalsha(this.script.getSha(), keys, argv);
                if((Long)response == 0) {
                    System.out.println(timestamp/1000 + ", false");
                    return false;
                }
            }
        }
        System.out.println(timestamp/1000 + ", true");
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
