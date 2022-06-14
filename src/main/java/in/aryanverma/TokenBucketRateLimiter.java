package in.aryanverma;

import in.aryanverma.limit.FixedWindowLimit;
import in.aryanverma.limit.Limit;
import in.aryanverma.limit.TokenBucketLimit;
import in.aryanverma.luascript.LuaScript;
import in.aryanverma.luascript.TokenBucketLuaScript;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenBucketRateLimiter extends RateLimiter{

    private final LuaScript script;

    public TokenBucketRateLimiter(JedisPool jedisPool){
        super(jedisPool);
        try(Jedis jedis = jedisPool.getResource()) {
            script = new TokenBucketLuaScript(jedis);
        }
    }
    @Override
    public boolean tryRequest(String identity, int cost) {
        boolean allow = true;
        long timestamp = System.currentTimeMillis();
        try(Jedis jedis = jedisPool.getResource()){

            List<Response<Object>> responseList = new ArrayList<>();
            Transaction transaction = jedis.multi();
            for(Limit limit: limits) {
                List<String> keys = Arrays.asList(RateLimiterUtility.getKey(identity, this.toString(), limit.toString()),
                        RateLimiterUtility.getTimestampKey(identity, this.toString(), limit.toString()));
                List<String> args = Arrays.asList(
                        Integer.toString(limit.getCapacity()),
                        Integer.toString(limit.getRate()),
                        Long.toString(timestamp),
                        Integer.toString(cost)
                );
                Response<Object> response = transaction.evalsha(script.getSha(), keys, args);
                responseList.add(response);
            }
            transaction.exec();
            for(Response<Object> response: responseList){
                if((Long)response.get()== 0) allow = false;
            }
        }
        System.out.println(timestamp/1000 + ", "+ allow);
        return allow;
    }

    @Override
    protected void checkLimitType(Limit limit) throws RateLimiterException{
        if(limit instanceof TokenBucketLimit) return;
        throw new RateLimiterException("Limit type is not TokenBucketLimit");
    }

    @Override
    public String toString() {
        return "tokenBucket";
    }
}
