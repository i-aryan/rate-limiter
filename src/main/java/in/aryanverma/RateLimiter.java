package in.aryanverma;

import in.aryanverma.limit.*;
import in.aryanverma.luascript.LuaScript;
import redis.clients.jedis.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class RateLimiter {

    protected final String rateLimiterId; // used to uniquely identify a rateLimiter within a RateLimiterManager. Used in redis key name.
    protected final JedisPool jedisPool;
    protected LuaScript script; // holds LuaScript instance used to get SHA of loaded script
    protected List<Limit> limits = new CopyOnWriteArrayList<>(); // Thread-safe list so new limits can be added at any time

    /**
     *
     * @param rateLimiterId String to uniquely identify a ratelimiter. Used in Redis key name.
     * @param jedisPool Jedispool instance to be used while fetching redis connections.
     */
    public RateLimiter(String rateLimiterId, JedisPool jedisPool){
        this.rateLimiterId = rateLimiterId;
        this.jedisPool = jedisPool;
        try(Jedis jedis = jedisPool.getResource()){
            this.script = createLuaScript(jedis);
        }
    }

    /**
     * Used to check whether request should be allowed or not.
     * @param identity To identify each user. Used in Redis key.
     * @param cost Cost/weight of the request.
     * @return True if the request is good to go, or false if the limit is reached.
     * @throws RateLimiterException
     */
    public abstract boolean tryRequest(String identity, int cost) throws RateLimiterException;

    /**
     * It's just tryRequest with cost as 1.
     * @param identity To identify each user. Used in Redis key.
     * @return
     * @throws RateLimiterException
     */
    public boolean tryRequest(String identity) throws RateLimiterException {
        return tryRequest(identity, 1);
    }

    /**
     * To add a limit to the rate limiter. Thread safe. Do not add more than 1 limit if leaky bucket.
     * @param limit Limit subclass that matches the rate limiter implementation.
     * @return returns instance of the rate limiter. Could be used for chaining.
     * @throws RateLimiterException
     */
    public RateLimiter addLimit(Limit limit) throws RateLimiterException {
        checkLimitType(limit);
        limits.add(limit);
        return this;
    }

    /**
     * Used to validate if rateLimiter subclass matches its corresponding limit subclass.
     * @param limit Limit subclass object to be checked against rateLimiter subclass.
     * @throws RateLimiterException
     */
    protected abstract void checkLimitType(Limit limit) throws RateLimiterException;

    /**
     * Used to create LuaScript subclass corresponding to RateLimiter class type and load it into Redis server.
     * @param jedis Jedis connection used to load LuaScript into redis server.
     * @return Returns newly created LuaScript object.
     */
    protected abstract LuaScript createLuaScript(Jedis jedis);

}
