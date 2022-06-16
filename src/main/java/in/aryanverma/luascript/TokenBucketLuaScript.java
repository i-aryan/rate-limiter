package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public class TokenBucketLuaScript extends LuaScript{
    private String script = "local tokensKey = KEYS[1]; " +
            "local timestampKey = KEYS[2]; " +
            " " +
            "local capacity = tonumber(ARGV[1]); " +
            "local rate = tonumber(ARGV[2]); " +
            "local timestamp = tonumber(ARGV[3]); " +
            "local cost = tonumber(ARGV[4]); " +
            " " +
            "local tokens = tonumber(redis.call('get', tokensKey)); " +
            "if tokens == nil then " +
            "tokens = capacity; " +
            "end " +
            " " +
            "local prevTimestamp = tonumber(redis.call('get', timestampKey)); " +
            "if prevTimestamp == nil then " +
            "prevTimestamp = 0; " +
            "end " +
            " " +
            "tokens = math.min(capacity, tokens + math.max(timestamp - prevTimestamp, 0)*rate/1000); " +
            " " +
            "local allow = 0; " +
            "if cost <= tokens then " +
            "tokens = tokens - cost; " +
            "allow = 1; " +
            "end " +
            " " +
            "redis.call('setex', tokensKey, math.ceil(2*capacity/rate), tokens); " +
            "redis.call('setex', timestampKey, math.ceil(2*capacity/rate), timestamp); " +
            " " +
            "return allow;";
    public TokenBucketLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
