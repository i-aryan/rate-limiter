package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public class LeakyBucketLuaScript extends LuaScript{
    private String script= "local keyName = KEYS[1]; " +
            "local timestampKey = KEYS[2]; " +
            " " +
            "local capacity = tonumber(ARGV[1]); " +
            "local dispatchInterval = math.ceil(1000/tonumber(ARGV[2])); " +
            "local timestamp = tonumber(ARGV[3]); " +
            "local setKeyName = ARGV[4]; " +
            "local cost = tonumber(ARGV[5]) " +
            "redis.call('zremrangebyscore', keyName, 0, timestamp); " +
            "local queueSize = tonumber(redis.call('zcard', keyName)); " +
            " " +
            "if queueSize + cost > capacity then " +
            "return {0, 0}; " +
            "end " +
            " " +
            "local lastDispatchTime = tonumber(redis.call('get', timestampKey)); " +
            "if lastDispatchTime == nil then " +
            "lastDispatchTime = 0; " +
            "end " +
            "local newDispatchTime = lastDispatchTime + dispatchInterval; " +
            "if newDispatchTime < timestamp then " +
            "newDispatchTime = timestamp " +
            "end " +
            "redis.call('setex', timestampKey, math.ceil(2*capacity*dispatchInterval/1000), newDispatchTime); " +
            "for i=1, cost do "+
            "setKeyName = setKeyName .. '*' "+
            "redis.call('zadd', keyName, newDispatchTime, setKeyName); " +
            "end "+
            "redis.call('expire', keyName, math.ceil(2*capacity*dispatchInterval/1000)); " +
            "return {1, newDispatchTime-timestamp}; ";

    public LeakyBucketLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
