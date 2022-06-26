package in.aryanverma.ratelimiter.luascript;

import redis.clients.jedis.Jedis;

public class FixedWindowLuaScript extends LuaScript{
    private String script = "local bucket = KEYS[1]; " +
            "local capacity = tonumber(ARGV[1]); " +
            "local period = tonumber(ARGV[2]); " +
            "local cost = tonumber(ARGV[3]); " +
            " " +
            "local currentCount = tonumber(redis.call('get', bucket)); " + // getting current bucket count
            "if currentCount == nil then " + // if redis returns nil, set count to 0
            "currentCount = 0; " +
            "end " +
            "if currentCount + cost > capacity then " + // check if adding current request exceeds capacity
            "return 0; " +
            "end " +
            "redis.call('incrby', bucket, cost); " + // incrementing counter by cost
            "redis.call('expire', bucket, 2*period) " + // setting expire time for the key
            "return 1;";

    public FixedWindowLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
