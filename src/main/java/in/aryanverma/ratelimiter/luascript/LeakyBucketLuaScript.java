package in.aryanverma.ratelimiter.luascript;

import redis.clients.jedis.Jedis;

public class LeakyBucketLuaScript extends LuaScript{
    private String script = "local keyName = KEYS[1]; " +
            "local timestampKey = KEYS[2]; " +
            " " +
            "local capacity = tonumber(ARGV[1]); " +
            "local dispatchInterval = math.ceil(1000/tonumber(ARGV[2])); " + // calculating dispatch interval from rate
            "local timestamp = tonumber(ARGV[3]); " +
            "local setKeyName = ARGV[4]; " +
            "local cost = tonumber(ARGV[5]) " +
            "redis.call('zremrangebyscore', keyName, 0, timestamp); " + // removing processed requests from queue
            "local queueSize = tonumber(redis.call('zcard', keyName)); " + // getting the count of pending requests
            " " +
            "if queueSize + cost > capacity then " + // checking if current request could be fit into queue
            "return {0, 0}; " + // if not, returning false
            "end " +
            " " +
            "local lastDispatchTime = tonumber(redis.call('get', timestampKey)); " + // fetching the dispatch time of last request in queue
            "if lastDispatchTime == nil then " + // if it returns nill, set last dispatch time 0
            "lastDispatchTime = 0; " +
            "end " +
            "local newDispatchTime = lastDispatchTime + dispatchInterval; " + // calculating dispatch time for current request
            "if newDispatchTime < timestamp then " + // if dispatch time is less then set to current time
            "newDispatchTime = timestamp " +
            "end " +
            "redis.call('setex', timestampKey, math.ceil(2*capacity*dispatchInterval/1000), newDispatchTime); " + // updating last dispatch time in redis
            "for i=1, cost do "+ //adding request to the queue (count is equal to cost)
            "setKeyName = setKeyName .. '*' "+
            "redis.call('zadd', keyName, newDispatchTime, setKeyName); " +
            "end "+
            "redis.call('expire', keyName, math.ceil(2*capacity*dispatchInterval/1000)); " + // updating expire time of queue
            "return {1, newDispatchTime-timestamp}; "; // return true and milliseconds to wait before processing current request

    public LeakyBucketLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
