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

    protected final JedisPool jedisPool;
    protected LuaScript script;
    protected List<Limit> limits = new ArrayList<>();

    public RateLimiter(JedisPool jedisPool){
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

    public static void main(String[] args) throws RateLimiterException{
        JedisPool jedisPool1 = new JedisPool("localhost", 6379);
        RateLimiter rateLimiter = RateLimiterManager.createRateLimiter(jedisPool1, RateLimiterType.SLIDING_LOG);
        rateLimiter.addLimit(new SlidingLogLimit("test", 4, Duration.ofSeconds(5)));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        Random random = new Random();
        for(int i=0; i<100; i++){
            PretendRequest request = new PretendRequest(rateLimiter,(1 + random.nextInt(10) )*1000);
            executor.execute(request);
        }
        executor.shutdown();
    }
}

class PretendRequest implements Runnable {
    private RateLimiter rateLimiter;
    private int sleepTime;
    public PretendRequest(RateLimiter rateLimiter, int sleepTime){
        this.rateLimiter = rateLimiter;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        try{
            Thread.sleep(this.sleepTime);
        }catch (InterruptedException e){}
        try {
            rateLimiter.tryRequest("Thread.currentThread().getName()", 1);
        }
        catch (RateLimiterException e){
            System.out.println(e);
        }
    }
}

