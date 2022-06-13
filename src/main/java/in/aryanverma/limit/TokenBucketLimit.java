package in.aryanverma.limit;

import java.time.Duration;

public class TokenBucketLimit extends Limit{
    public TokenBucketLimit(String limitId, Integer capacity, Integer refillRate){
        super(limitId, capacity, Duration.ofSeconds(1), refillRate, 0);
    }
}
