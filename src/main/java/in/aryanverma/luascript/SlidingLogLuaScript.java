package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public class SlidingLogLuaScript extends LuaScript{
    private String script= "local keyName = KEYS[1]; " +
            "local timestamp = tonumber(ARGV[1]); " +
            "local setKeyName = ARGV[2]; " +
            "local cost = tonumber(ARGV[3]); " +
            " " +
            "local maxPeriod = 0; " +
            " " +
            "for i=4, #ARGV, 2 do " +
            "local counter = redis.call('zcount', keyName, timestamp - tonumber(ARGV[i+1])*1000 + 1, timestamp+10000); " +
            "if counter + cost > tonumber(ARGV[i]) then " +
            "return 0; " +
            "end " +
            "maxPeriod = math.max(maxPeriod, tonumber(ARGV[i+1])); " +
            "end " +
            "redis.call('zremrangebyscore', keyName, 0, timestamp - maxPeriod*1000-10000); " +
            "for i=1, cost do " +
            "setKeyName = setKeyName .. '*' " +
            "redis.call('zadd', keyName, timestamp, setKeyName); " +
            "end " +
            "redis.call('expire', keyName, maxPeriod); " +
            "return 1;";

    public SlidingLogLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
