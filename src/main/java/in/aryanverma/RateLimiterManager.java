package in.aryanverma;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RateLimiterManager {
    public static RateLimiter createRateLimiter(JedisPool jedisPool, RateLimiterType type){
        if(type == RateLimiterType.FIXED_WINDOW) return new FixedWindowRateLimiter(jedisPool);
        if(type == RateLimiterType.LEAKY_BUCKET) return new LeakyBucketRateLimiter(jedisPool);
        if(type == RateLimiterType.SLIDING_LOG) return new SlidingLogRateLimiter(jedisPool);
        if(type == RateLimiterType.SLIDING_WINDOW) return new SlidingWidowRateLimiter(jedisPool);
        return new TokenBucketRateLimiter(jedisPool);
    }
}
