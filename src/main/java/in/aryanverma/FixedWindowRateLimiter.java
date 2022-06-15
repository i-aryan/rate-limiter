package in.aryanverma;

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

    protected LuaScript script;
    public FixedWindowRateLimiter(JedisPool jedisPool) {
        super(jedisPool);
        try (Jedis jedis = jedisPool.getResource()) {
            script = new FixedWindowLuaScript(jedis);

        }
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException {
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
        long timestamp = System.currentTimeMillis()/1000;
        try(Jedis jedis = this.jedisPool.getResource()){
            for(Limit limit: this.limits){
                long bucket = (timestamp/limit.getPeriod().getSeconds())*(limit.getPeriod().getSeconds());
                String key = RateLimiterUtility.getKeyWithTimestamp(identity, this.toString(), limit.toString(), bucket);
                List<String> argv = Arrays.asList(limit.getCapacity().toString(), Long.toString(limit.getPeriod().getSeconds()), Integer.toString(cost));
                Object counter = jedis.evalsha(script.getSha(), Arrays.asList(key), argv);
                if((Long)counter == 0) {
                    System.out.println(timestamp/3 + ", false, " + timestamp/6);
                    return false;
                }
            }
        }
        System.out.println(timestamp/3 + ", true, " + timestamp/6);
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
