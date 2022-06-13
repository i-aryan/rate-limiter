package in.aryanverma.limit;

import java.time.Duration;

public class Limit {
    private Integer capacity;
    private Integer refillRate;
    private Integer lookBackCount;
    private Duration period;

    private String limitId;

    public Limit(String limitId, Integer capacity, Duration period, Integer refillRate, Integer lookBackCount){
        this.limitId = limitId;
        this.capacity = capacity;
        this.period = period;
        this.refillRate = refillRate;
        this.lookBackCount = lookBackCount;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(Integer refillRate) {
        this.refillRate = refillRate;
    }

    public Integer getLookBackCount() {
        return lookBackCount;
    }

    public void setLookBackCount(Integer lookBackCount) {
        this.lookBackCount = lookBackCount;
    }

    public Duration getPeriod() {
        return period;
    }

    public void setPeriod(Duration period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return limitId;
    }
}

