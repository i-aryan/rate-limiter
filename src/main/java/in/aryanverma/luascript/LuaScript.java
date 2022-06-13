package in.aryanverma.luascript;

import redis.clients.jedis.Jedis;

public abstract class LuaScript {
    private String sha;
    protected void loadScript(Jedis jedis, String script){
        this.sha = jedis.scriptLoad(script);
    }

    public String getSha() {
        return sha;
    }

}
