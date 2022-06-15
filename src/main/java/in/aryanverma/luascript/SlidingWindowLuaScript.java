package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public class SlidingWindowLuaScript extends LuaScript {
    private String script = "local keyName = KEYS[1]; " +
            "local capacity = tonumber(ARGV[1]); " +
            "local period = tonumber(ARGV[2]); " +
            "local lookbackCount = tonumber(ARGV[3]); " +
            "local timestamp = tonumber(ARGV[4]); " +
            "local cost = tonumber(ARGV[5]); " +
            " " +
            "local bucket = math.floor(timestamp/period)*period; " +
            "local keys = redis.call('hkeys', keyName); " +
            "for i=1, #keys do " +
            "if (tonumber(keys[i]) < bucket - lookbackCount*period) then " +
            "redis.call('hdel', keyName, keys[i]); " +
            "end " +
            "end " +
            " " +
            "local vals = redis.call('hvals', keyName); " +
            "local sum = 0; " +
            "for i=1, #vals do " +
            "sum = sum + vals[i]; " +
            "end " +
            "local extraBucket = tonumber(redis.call('hget', keyName, tostring(bucket-lookbackCount*period))); " +
            "if extraBucket ~=nil then " +
            "sum = sum-math.floor(extraBucket/period)*(timestamp - bucket); " +
            "end " +
            "if sum + cost>capacity then " +
            "return 0; " +
            "end " +
            "redis.call('hincrby', keyName, tostring(bucket), cost); " +
            "redis.call('expire', keyName, math.floor((lookbackCount+2)*period/1000)); " +
            "return 1; ";

    public SlidingWindowLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
