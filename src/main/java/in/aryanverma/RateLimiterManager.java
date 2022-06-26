package in.aryanverma;

import redis.clients.jedis.JedisPool;

import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterManager {

    ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>(); // stores all rate limiters

    /**
     * Create a rate limiter with given type and id.
     * @param rateLimiterId Used to uniquely identify each rate limiter. Can be used to get rateLimiter. Used in redis key name.
     * @param type RateLimiter type. Defined in RateLimiterType enum.
     * @param jedisPool JedisPool to be used to get connections.
     * @return returns the rateLimiter instance of given type.
     * @throws RateLimiterException if rateLimiter with given Id already exists.
     */
    public RateLimiter createRateLimiter(String rateLimiterId, RateLimiterType type, JedisPool jedisPool) throws RateLimiterException{
        if(rateLimiters.get(rateLimiterId) != null) throw new RateLimiterException("Rate Limiter with given Id already exists");

        RateLimiter rateLimiter;
        if(type == RateLimiterType.FIXED_WINDOW) rateLimiter = new FixedWindowRateLimiter(rateLimiterId, jedisPool);
        else if(type == RateLimiterType.LEAKY_BUCKET) rateLimiter = new LeakyBucketRateLimiter(rateLimiterId, jedisPool);
        else if(type == RateLimiterType.SLIDING_LOG) rateLimiter = new SlidingLogRateLimiter(rateLimiterId, jedisPool);
        else if(type == RateLimiterType.SLIDING_WINDOW) rateLimiter = new SlidingWidowRateLimiter(rateLimiterId, jedisPool);
        else rateLimiter = new TokenBucketRateLimiter(rateLimiterId, jedisPool);

        rateLimiters.put(rateLimiterId, rateLimiter);
        return rateLimiter;
    }

    /**
     * Get rateLimiter with given Id.
     * @param rateLimiterId
     * @return null if rateLimiter doesn't exist, otherwise returns RateLimiter object.
     */
    public RateLimiter getRateLimiter(String rateLimiterId){
        return rateLimiters.get(rateLimiterId);
    }
}
