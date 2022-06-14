package in.aryanverma.limit;

import java.time.Duration;

public class LeakyBucketLimit extends Limit{
    public LeakyBucketLimit(String limitId, Integer capacity, Integer Rate){
        super(limitId, capacity, Duration.ofSeconds(1), Rate, 0);
    }
}
