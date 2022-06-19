package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.limit.SlidingLogLimit;
import in.aryanverma.luascript.FixedWindowLuaScript;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.SlidingLogLuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlidingLogRateLimiter extends RateLimiter{


    public SlidingLogRateLimiter(JedisPool jedisPool){
        super(jedisPool);
    }

    protected LuaScript createLuaScript(Jedis jedis) {
        return new SlidingLogLuaScript(jedis);
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException{
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){
            String key = RateLimiterUtility.getKey(identity, this.toString(), "nill");
            List<String> argv = new ArrayList<>();
            argv.add(Long.toString(timestamp));
            argv.add(RateLimiterUtility.getKeyWithRandomNumber(timestamp));
            argv.add(Integer.toString(cost));
            for(Limit limit: this.limits){
                argv.add(limit.getCapacity().toString());
                argv.add(Long.toString(limit.getPeriod().getSeconds()));
            }
            Object response = jedis.evalsha(this.script.getSha(), Arrays.asList(key), argv);
            if((Long)response==0) {
//                System.out.println(timestamp/1000 + ",false " + timestamp);
                return false;
            }
//            System.out.println(timestamp/1000 + ",true " + timestamp);
        }
        return true;
    }

    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof SlidingLogLimit) return;
        throw new RateLimiterException("Limit type is not SlidingLogLimit");
    }

    @Override
    public String toString() {
        return "slidingLog";
    }
}
