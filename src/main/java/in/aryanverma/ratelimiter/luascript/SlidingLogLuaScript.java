package in.aryanverma.ratelimiter.luascript;

import redis.clients.jedis.Jedis;

public class SlidingLogLuaScript extends LuaScript{
    private String script = "local keyName = KEYS[1]; " +
            "local timestamp = tonumber(ARGV[1]); " +
            "local setKeyName = ARGV[2]; " +
            "local cost = tonumber(ARGV[3]); " +
            " " +
            "local maxPeriod = 0; " +
            " " +
            "for i=4, #ARGV, 2 do " + // iterating through limits to check if current request is allowed by them
            "local timeperiod = tonumber(ARGV[i+1]);"+ // timeperiod of current limit
            "local capacity = tonumber(ARGV[i]);"+ // capacity of current limit
            "local counter = redis.call('zcount', keyName, timestamp - timeperiod*1000 + 1, timestamp+5000); " + // count of requests in limit duration
            "if counter + cost > capacity then " + // checking if adding this request exceeds this limits's capacity
            "return 0; " +
            "end " +
            "maxPeriod = math.max(maxPeriod, timeperiod); " + // updating max period
            "end " +
            "redis.call('zremrangebyscore', keyName, 0, timestamp - maxPeriod*1000-5000); " + // removing older requests that won't be required
            "for i=1, cost do " + // adding current request to the sorted set
            "setKeyName = setKeyName .. '*' " +
            "redis.call('zadd', keyName, timestamp, setKeyName); " +
            "end " +
            "redis.call('expire', keyName, maxPeriod); " + // updating expiry time of sorted set
            "return 1;";


    public SlidingLogLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
