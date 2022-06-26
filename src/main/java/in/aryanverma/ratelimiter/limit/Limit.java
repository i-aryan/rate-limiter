package in.aryanverma.ratelimiter.limit;

import java.time.Duration;

public abstract class Limit {
    private Integer capacity; // capacity for the limit
    private Integer Rate; // refill rate or dispatch rate
    private Integer lookBackCount; // look back count of buckets used for sliding window
    private Duration period; // rate limiting interval duration
    private String limitId; // limit id to uniquely identify limits, used in redis key

    public Limit(String limitId, Integer capacity, Duration period, Integer Rate, Integer lookBackCount){
        this.limitId = limitId;
        this.capacity = capacity;
        this.period = period;
        this.Rate = Rate;
        this.lookBackCount = lookBackCount;
    }

    public String getLimitId() {
        return limitId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getRate() {
        return Rate;
    }

    public void setRate(Integer refillRate) {
        this.Rate = refillRate;
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

