package in.aryanverma.ratelimiter.exception;

/**
 * Is thrown when trying to add rateLimiter with ID that already exists.
 */
public class DuplicateRateLimiterIdException extends RateLimiterException {
    public DuplicateRateLimiterIdException(String err){
        super(err);
    }
}
