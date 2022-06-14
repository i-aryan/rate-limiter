package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;

public class FixedWindowRateLimiter extends RateLimiter{

    public FixedWindowRateLimiter(JedisPool jedisPool){
        super(jedisPool);
    }
    @Override
    public boolean tryRequest(String identity, int cost) throws RateLimiterException {
        if(limits.isEmpty()) throw new RateLimiterException("Limit is empty");
//        List<Long> buckets = new ArrayList<>();
        boolean allow = true;
        long timestamp = System.currentTimeMillis()/1000;
        try(Jedis jedis = this.jedisPool.getResource()){
            int index = 0;
            Transaction transaction = jedis.multi();
            List<Response<Long>> responseList = new ArrayList<>();
            for(Limit limit: this.limits){
                long bucket = (timestamp/limit.getPeriod().getSeconds())*(limit.getPeriod().getSeconds());
//                buckets.add(bucket);
                String key = RateLimiterUtility.getKeyWithTimestamp(identity, this.toString(), limit.toString(), bucket);
                Response<Long> counter = transaction.incrBy(key, cost);
                responseList.add(counter);
                transaction.expire(key, 2*limit.getPeriod().getSeconds());
            }
            transaction.exec();
            index = 0;
            for(Limit limit: this.limits){
                if(responseList.get(index++).get() > limit.getCapacity()) allow = false;
            }
        }
        //System.out.println(buckets.get(0) + ", " + buckets.get(1) + "," + allow);
        return allow;
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
