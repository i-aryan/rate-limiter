package in.aryanverma.ratelimiter.luascript;

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
            "local tokens = tonumber(redis.call('get', tokensKey)); " + // getting current number of tokens
            "if tokens == nil then " + // if it returns nil, then set tokens to capacity (key expired after capacity reached or new user)
            "tokens = capacity; " +
            "end " +
            " " +
            "local prevTimestamp = tonumber(redis.call('get', timestampKey)); " + // getting the last timestamp tokens were fetched
            "if prevTimestamp == nil then " +
            "prevTimestamp = 0; " +
            "end " +
            " " +
            "tokens = math.min(capacity, tokens + math.max(timestamp - prevTimestamp, 0)*rate/1000); " + // using the difference between current and last timestamp to calculate tokens to refill
            " " +
            "local allow = 0; " +
            "if cost <= tokens then " +
            "tokens = tokens - cost; " + // if tokens can be consumed, reduce them
            "allow = 1; " +
            "end " +
            " " +
            "redis.call('setex', tokensKey, math.ceil(2*capacity/rate), tokens); " + // updating tokens bucket and its expiry time
            "redis.call('setex', timestampKey, math.ceil(2*capacity/rate), timestamp); " + // updating last timestamp key and the expiry time
            " " +
            "return allow;";

    public TokenBucketLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
