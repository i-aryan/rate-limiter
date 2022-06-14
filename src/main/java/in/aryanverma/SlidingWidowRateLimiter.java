package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.limit.SlidingWindowLimit;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.SlidingWindowLuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

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
        boolean allow = true;
        try(Jedis jedis = jedisPool.getResource()){
            List<Response<Object>> responseList = new ArrayList<>();
            Transaction transaction = jedis.multi();
            int index = 0;
            for(Limit limit: this.limits){
                String key = RateLimiterUtility.getKey(identity, this.toString(), limit.toString());
                List<String> keys = Arrays.asList(key);
                List<String> argv = Arrays.asList(limit.getCapacity().toString(), Long.toString(limit.getPeriod().getSeconds()*1000), limit.getLookBackCount().toString(), Long.toString(timestamp));
                Response<Object> response= transaction.evalsha(this.script.getSha(), keys, argv);
                responseList.add(response);
            }
            transaction.exec();
            for(Response<Object> response: responseList){
                if((Long)response.get() == 0) allow = false;
            }
            System.out.println(timestamp/1000 + ", " + allow);
        }
        return  allow;
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
