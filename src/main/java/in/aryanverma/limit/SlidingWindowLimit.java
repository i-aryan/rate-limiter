package in.aryanverma.limit;

import java.time.Duration;

public class SlidingWindowLimit extends Limit{
    public SlidingWindowLimit(String limitId, Integer capacity, Duration period, Integer lookBackCount){
        super(limitId, capacity, period, 0, lookBackCount, 0);
    }
}
