package in.aryanverma.limit;

import in.aryanverma.limit.Limit;

import java.time.Duration;

public class FixedWindowLimit extends Limit {

    public FixedWindowLimit(String limitId, Integer capacity, Duration period){
        super(limitId, capacity, period,0, 0, 0);
    }
}