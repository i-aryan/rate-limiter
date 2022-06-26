package in.aryanverma.exception;

/**
 * Is thrown if more than 1 limit is there in LeakyBucket RateLimiter
 */
public class MultipleLimitLeakyBucketException extends RateLimiterException{
    public MultipleLimitLeakyBucketException(String err){
        super(err);
    }
}
