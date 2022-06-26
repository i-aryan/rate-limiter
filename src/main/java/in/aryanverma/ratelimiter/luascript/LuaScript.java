package in.aryanverma.ratelimiter.luascript;

import redis.clients.jedis.Jedis;

public abstract class LuaScript {
    private String sha; // stores the SHA received by loading the lua script into redis server

    /**
     * loads lua script into redis server and sets sha instance member
     * @param jedis jedis connection
     * @param script lua script to be loaded
     */
    protected void loadScript(Jedis jedis, String script){
        this.sha = jedis.scriptLoad(script);
    }

    /**
     * @return SHA of loaded lua script.
     */
    public String getSha() {
        return sha;
    }

}
