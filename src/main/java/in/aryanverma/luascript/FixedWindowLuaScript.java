package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public class FixedWindowLuaScript extends LuaScript{
    private String script= "local bucket = KEYS[1]; " +
            "local capacity = tonumber(ARGV[1]); " +
            "local period = tonumber(ARGV[2]); " +
            "local cost = tonumber(ARGV[3]); " +
            " " +
            "local currentCount = tonumber(redis.call('get', bucket)); " +
            "if currentCount == nil then " +
            "currentCount = 0; " +
            "end " +
            "if currentCount + cost > capacity then " +
            "return 0; " +
            "end " +
            "redis.call('incrby', bucket, cost); " +
            "redis.call('expire', bucket, 2*period) " +
            "return 1;";

    public FixedWindowLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
