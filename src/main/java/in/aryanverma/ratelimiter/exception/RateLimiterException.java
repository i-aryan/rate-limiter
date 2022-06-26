package in.aryanverma.ratelimiter.exception;

public class RateLimiterException extends Exception {
    public RateLimiterException(String error){
        super(error);
    }
}