package in.aryanverma.ratelimiter;

import in.aryanverma.ratelimiter.exception.DuplicateLimitIdException;
import in.aryanverma.ratelimiter.exception.LimitMismatchException;
import in.aryanverma.ratelimiter.exception.RateLimiterException;
import in.aryanverma.ratelimiter.limit.Limit;
import in.aryanverma.ratelimiter.luascript.LuaScript;
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
     * @param limit limit Limit subclass that matches the rate limiter implementation.
     * @return returns instance of the rate limiter. Could be used for chaining.
     * @throws DuplicateLimitIdException if limit with given Id already exists.
     * @throws LimitMismatchException if there's limit class is not compatible with rateLimiter class.
     */
    public synchronized RateLimiter addLimit(Limit limit) throws DuplicateLimitIdException, LimitMismatchException {
        checkLimitType(limit);
        for(Limit currLimit: this.limits){
            //check if limit with given Id already exists
            if(currLimit.getLimitId() == limit.getLimitId()) throw new DuplicateLimitIdException("Limit with given ID already exists.");
        }
        limits.add(limit);
        return this;
    }

    /**
     * Deletes all the limits.
     */
    public void removeAllLimits(){
        this.limits.clear();
    }

    /**
     * @return Returns the limits array list. Actual type is CopyOnWriteArrayList.
     */
    public List<Limit> getLimits(){
        return this.limits;
    }

    /**
     * Used to validate if rateLimiter subclass matches its corresponding limit subclass.
     * @param limit Limit subclass object to be checked against rateLimiter subclass.
     * @throws RateLimiterException if there's a limit type mismatch against rateLimiter class
     */
    protected abstract void checkLimitType(Limit limit) throws LimitMismatchException;

    /**
     * Used to create LuaScript subclass corresponding to RateLimiter class type and load it into Redis server.
     * @param jedis Jedis connection used to load LuaScript into redis server.
     * @return Returns newly created LuaScript object.
     */
    protected abstract LuaScript createLuaScript(Jedis jedis);

}
