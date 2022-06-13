package in.aryanverma;

import redis.clients.jedis.JedisPool;

public class LeakyBucketRateLimiter extends RateLimiter{
    public LeakyBucketRateLimiter(JedisPool jedisPool) {
        super(jedisPool);
    }
    @Override
    public boolean tryRequest(String identity, int cost) {
        return true;
    }
}
