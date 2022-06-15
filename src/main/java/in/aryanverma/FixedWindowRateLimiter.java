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
        long timestamp = System.currentTimeMillis()/1000;
        try(Jedis jedis = this.jedisPool.getResource()){
            int index = 0;
            for(Limit limit: this.limits){
                long bucket = (timestamp/limit.getPeriod().getSeconds())*(limit.getPeriod().getSeconds());
                String key = RateLimiterUtility.getKeyWithTimestamp(identity, this.toString(), limit.toString(), bucket);
                Long counter = jedis.incrBy(key, cost);
                jedis.expire(key, 2*limit.getPeriod().getSeconds());
                if(counter>limit.getCapacity()) {
                    return false;
                }
            }
        }
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
