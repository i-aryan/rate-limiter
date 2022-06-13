package in.aryanverma;

public class RateLimiterError extends Exception {
    public RateLimiterError(String error){
        super(error);
    }
}
