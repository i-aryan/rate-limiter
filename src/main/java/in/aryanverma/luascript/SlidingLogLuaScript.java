package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public class SlidingLogLuaScript extends LuaScript{
    private String script= "";

    public SlidingLogLuaScript(Jedis jedis){
        loadScript(jedis, script);
    }
}
