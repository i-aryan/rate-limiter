package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.limit.SlidingLogLimit;
import in.aryanverma.luascript.FixedWindowLuaScript;
import in.aryanverma.luascript.LuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;

public class SlidingLogRateLimiter extends RateLimiter{


    public SlidingLogRateLimiter(JedisPool jedisPool){
        super(jedisPool);
    }

    protected LuaScript createLuaScript(Jedis jedis) {
        return new FixedWindowLuaScript(jedis);
    }
    @Override
    public boolean tryRequest(String identity, int cost) {
        long timestamp = System.currentTimeMillis();
        boolean allow = true;
        try(Jedis jedis = jedisPool.getResource()){
//            long temptime = System.currentTimeMillis();
            List<Response<Long>> responseList =  new ArrayList<>();
            Transaction transaction = jedis.multi();
            String key = RateLimiterUtility.getKey(identity, this.toString(), "nill");
            transaction.zadd(key, timestamp, RateLimiterUtility.getKeyWithRandomNumber(timestamp));
            long maxPeriod = 0;
            for(Limit limit: this.limits){
                maxPeriod = Math.max(maxPeriod, limit.getPeriod().getSeconds());
                Response<Long> response = transaction.zcount(key, timestamp-limit.getPeriod().getSeconds()*1000+1, timestamp+10000);
                responseList.add(response);
            }
            transaction.zremrangeByScore(key, 0, timestamp - maxPeriod*1000 - 10000);
            transaction.expire(key, maxPeriod);
            transaction.exec();
            int index = 0;
            for(Limit limit: this.limits){
                if(responseList.get(index++).get() > limit.getCapacity()) allow = false;
            }
//           System.out.println(timestamp + ", " + allow + ", count:"+responseList.get(0).get() +", timestamp enter:" +temptime);
            System.out.println(timestamp/1000 + ","+ allow);
        }
        return allow;
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
