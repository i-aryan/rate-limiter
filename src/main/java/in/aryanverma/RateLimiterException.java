package in.aryanverma;

public class RateLimiterException extends Exception {
    public RateLimiterException(String error){
        super(error);
    }
}