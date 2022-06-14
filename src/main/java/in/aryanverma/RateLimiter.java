package in.aryanverma;

import in.aryanverma.limit.*;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class RateLimiter {

    protected final JedisPool jedisPool;
    protected List<Limit> limits = new ArrayList<>();

    public RateLimiter(JedisPool jedisPool){
        this.jedisPool = jedisPool;
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

    public static void main(String[] args) throws RateLimiterException{
        JedisPool jedisPool1 = new JedisPool("localhost", 6379);
        RateLimiter rateLimiter = RateLimiterManager.createRateLimiter(jedisPool1, RateLimiterType.LEAKY_BUCKET);
        rateLimiter.addLimit(new LeakyBucketLimit("test", 5, 2));

        ExecutorService executor = Executors.newFixedThreadPool(10);
        Random random = new Random();
        for(int i=0; i<10000; i++){
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
            rateLimiter.tryRequest("Thread.currentThread().getName()");
        }
        catch (RateLimiterException e){
            System.out.println(e);
        }
    }
}

