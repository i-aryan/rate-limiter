package in.aryanverma;

import java.util.Random;

public class RateLimiterUtility {

    private static Random randomNumber= new Random();
    public static String getKeyWithTimestamp(String identity, String rateLimiterType, String limitId, long timestamp){
        return identity + ":" + rateLimiterType + ":" + limitId + ":" + timestamp;
    }

    public static String getKey(String identity, String rateLimiterType, String limitId){
        return identity + ":" + rateLimiterType + ":" + limitId;
    }
    public static String getTimestampKey(String identity, String rateLimiterType, String limitId){
        return identity + ":timestamp:" + rateLimiterType + ":" + limitId;
    }

    public static String getKeyWithRandomNumber(long timestamp){
        return timestamp + ":" + randomNumber.nextInt(100);
    }
}
