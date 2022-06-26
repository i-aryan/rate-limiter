package in.aryanverma.exception;

public class RateLimiterException extends Exception {
    public RateLimiterException(String error){
        super(error);
    }
}