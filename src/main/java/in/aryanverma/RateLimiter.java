package in.aryanverma;

import in.aryanverma.limit.*;
import in.aryanverma.luascript.LuaScript;
import redis.clients.jedis.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RateLimiter {

    protected final String rateLimiterId;
    protected final JedisPool jedisPool;

    protected LuaScript script;
    protected List<Limit> limits = new ArrayList<>();

    public RateLimiter(String rateLimiterId, JedisPool jedisPool){
        this.rateLimiterId = rateLimiterId;
        this.jedisPool = jedisPool;
        try(Jedis jedis = jedisPool.getResource()){
            this.script = createLuaScript(jedis);
        }
    }
    public abstract boolean tryRequest(String identity, int cost) throws RateLimiterException;

    public boolean tryRequest(String identity) throws RateLimiterException {
        return tryRequest(identity, 1);
    }

    public RateLimiter addLimit(Limit limit) throws RateLimiterException {
        checkLimitType(limit);
        synchronized (this.limits) {
            limits.add(limit);
        }
        return this;
    }

    protected abstract void checkLimitType(Limit limit) throws RateLimiterException;
    protected abstract LuaScript createLuaScript(Jedis jedis);


}
