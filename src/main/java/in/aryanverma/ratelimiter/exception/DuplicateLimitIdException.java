package in.aryanverma.ratelimiter.exception;

/**
 * Is thrown when trying to add limit with an ID that already exists.
 */
public class DuplicateLimitIdException extends RateLimiterException{
    public DuplicateLimitIdException(String err){
        super(err);
    }
}
