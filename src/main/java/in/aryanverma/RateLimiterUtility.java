package in.aryanverma;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RateLimiterUtility {

    public static String getKeyWithTimestamp(String identity, String rateLimiterId, String limitId, long timestamp){
        return identity + ":" + rateLimiterId + ":" + limitId + ":" + timestamp;
    }

    public static String getKey(String identity, String rateLimiterId, String limitId){
        return identity + ":" + rateLimiterId + ":" + limitId;
    }

    public static String getTimestampKey(String identity, String rateLimiterId, String limitId){
        return identity + ":timestamp:" + rateLimiterId + ":" + limitId;
    }

    public static String getKeyWithRandomNumber(long timestamp){
        return timestamp + ":" + ThreadLocalRandom.current().nextInt(100);
    }
}
