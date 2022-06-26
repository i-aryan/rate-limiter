package in.aryanverma.ratelimiter.exception;

/**
 * Is thrown if Limit type and RateLimiter class type are not compatible.
 */
public class LimitMismatchException extends RateLimiterException {
    public LimitMismatchException(String err){
        super(err);
    }
}
