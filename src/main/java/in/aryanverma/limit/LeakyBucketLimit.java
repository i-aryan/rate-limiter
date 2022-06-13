package in.aryanverma.limit;

import java.time.Duration;

public class LeakyBucketLimit extends Limit{
    public LeakyBucketLimit(String limitId, Integer capacity, Duration period){
        super(limitId, capacity, period, 0, 0);
    }
}
