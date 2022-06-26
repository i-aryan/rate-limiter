package in.aryanverma.exception;

/**
 * Is thrown if Limit list is empty.
 */
public class LimitsEmptyException extends RateLimiterException {
    public LimitsEmptyException(String err){
        super(err);
    }
}
