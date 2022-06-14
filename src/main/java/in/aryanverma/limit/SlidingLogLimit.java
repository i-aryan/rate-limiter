package in.aryanverma.limit;

import java.time.Duration;

public class SlidingLogLimit extends Limit{
    public SlidingLogLimit(String limitId, Integer capacity, Duration period){
        super(limitId, capacity, period, 0, 0, 0);
    }
}
