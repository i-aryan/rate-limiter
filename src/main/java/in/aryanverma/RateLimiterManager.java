package in.aryanverma;

import redis.clients.jedis.JedisPool;

public class RateLimiterManager {
    public static RateLimiter createRateLimiter(String rateLimiterId, RateLimiterType type, JedisPool jedisPool){
        if(type == RateLimiterType.FIXED_WINDOW) return new FixedWindowRateLimiter(rateLimiterId, jedisPool);
        if(type == RateLimiterType.LEAKY_BUCKET) return new LeakyBucketRateLimiter(rateLimiterId, jedisPool);
        if(type == RateLimiterType.SLIDING_LOG) return new SlidingLogRateLimiter(rateLimiterId, jedisPool);
        if(type == RateLimiterType.SLIDING_WINDOW) return new SlidingWidowRateLimiter(rateLimiterId, jedisPool);
        return new TokenBucketRateLimiter(rateLimiterId, jedisPool);
    }
}
