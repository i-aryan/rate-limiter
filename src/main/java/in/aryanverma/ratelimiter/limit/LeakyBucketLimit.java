package in.aryanverma.ratelimiter.limit;

import java.time.Duration;

public class LeakyBucketLimit extends Limit{
    public LeakyBucketLimit(String limitId, Integer capacity, Integer rate){
        super(limitId, capacity, Duration.ofSeconds(1), rate, 0);
    }
}
