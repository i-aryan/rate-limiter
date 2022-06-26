package in.aryanverma.ratelimiter.luascript;

import redis.clients.jedis.Jedis;

public class SlidingWindowLuaScript extends LuaScript {
    private String script = "local keyName = KEYS[1]; " +
            "local capacity = tonumber(ARGV[1]); " +
            "local period = tonumber(ARGV[2]); " +
            "local lookbackCount = tonumber(ARGV[3]); " +
            "local timestamp = tonumber(ARGV[4]); " +
            "local cost = tonumber(ARGV[5]); " +
            " " +
            "local bucket = math.floor(timestamp/period)*period; " + // calculating the bucket (in milliseconds) request falls into
            "local keys = redis.call('hkeys', keyName); " + // getting all the keys in hashmap
            "for i=1, #keys do " +
            "if (tonumber(keys[i]) < bucket - lookbackCount*period) then " + // check if this key should be deleted/lies beyond lookback count
            "redis.call('hdel', keyName, keys[i]); " + // deleting the key
            "end " +
            "end " +
            " " +
            "local vals = redis.call('hvals', keyName); " + // getting all the values in hashmap
            "local sum = 0; " +
            "for i=1, #vals do " +
            "sum = sum + vals[i]; " + // summing the values
            "end " +
            "local extraBucket = tonumber(redis.call('hget', keyName, tostring(bucket-lookbackCount*period))); " + // getting the (lookback count + 1)th bucket
            "if extraBucket ~=nil then " +
            "sum = sum-math.floor(extraBucket/period)*(timestamp - bucket); " + // if the bucket is there, then deleting extra requests in sum by assuming a constant rate of requests
            "end " +
            "if sum + cost>capacity then " + // checking if adding request exceeds capacity
            "return 0; " +
            "end " +
            "redis.call('hincrby', keyName, tostring(bucket), cost); " + // incrementing count of current bucket by cost
            "redis.call('expire', keyName, math.floor((lookbackCount+2)*period/1000)); " + // updating expire time of hashmap
            "return 1; ";

    public SlidingWindowLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
