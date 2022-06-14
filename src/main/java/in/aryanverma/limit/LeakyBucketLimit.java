package in.aryanverma.limit;

import java.time.Duration;

public class LeakyBucketLimit extends Limit{
    public LeakyBucketLimit(String limitId, Integer capacity, Integer dispatchInterval){
        super(limitId, capacity, Duration.ofSeconds(1), 0, 0, dispatchInterval);
    }
}
