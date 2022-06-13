package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public class TokenBucketLuaScript extends LuaScript{
    private String script = "local bucket_key = KEYS[1];"+
            "local timestamp_key = KEYS[2];"+
            "local capacity = tonumber(ARGV[1]);"+
            "local rate = tonumber(ARGV[2]);" +
            "local currentTimestamp = tonumber(ARGV[3]);"+
            "local cost = tonumber(ARGV[4]);"+
            "local ttl = 2*math.ceil(capacity/rate);"+
            "local prevTimestamp = tonumber(redis.call('get', timestamp_key));"+
            "if prevTimestamp == nil then "+
            "prevTimestamp = 0; "+
            "end "+
            "local tokens = tonumber(redis.call('get', bucket_key));"+
            "if tokens == nil then "+
            "tokens = capacity; "+
            "end "+
            "tokens = math.min(capacity, tokens+math.floor(((currentTimestamp-prevTimestamp)*rate)/1000));"+
            "local allowed = tokens >= cost;"+
            "if allowed then "+
            "tokens = tokens - cost;"+
            "end "+
            "redis.call('setex', bucket_key,ttl, tokens);"+
            "redis.call('setex', timestamp_key, ttl, currentTimestamp);"+
            "if allowed then "+
            "return 1;"+
            "end " +
            "return 0;";
    public TokenBucketLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
