# Rate Limiter
API Rate Limiter library built in Java. Uses Redis for the central datastore to make it compatible with distributed systems.

## Introduction
A Rate limiter is used to regulate the number of requests your API services are receiving. For example, having rate limits of 100 requests per minute and 1000 requests per hour on some APIs you provide to your service customers. This library offers 5 different algorithmic implementations of the Rate limiter namely
- Token Bucket
- Fixed Window
- Sliding Log
- Sliding Window
- Leaky Bucket

To understand how these algorithms work you can have a look at this really great [blog](https://www.enjoyalgorithms.com/blog/design-api-rate-limiter). Every implementation with an exception of Leaky Bucket allows you to add multiple limits which could be used for regulating requests per second/minute/hour/day or any duration that you want to put a limit on. It also allows you to have a cost associated with an API call so a single instance of rate limiter could be used on APIs demanding different rate limits by adjusting the cost.

### Dependencies
This project uses **Jedis**, a Redis client library for Java to make connections to Redis and issue queries.

### Limits

For every implementation, we have separate limit types to be used with them as they require different parameters.

```java
1. TokenBucketLimit(String limitId, Integer capacity, Integer refillRate)
```

- **limitId**: defines the unique name/id associated with every limit.

- **capacity**: Capacity of the limit/bucket.

- **refillRate**: It is the rate at which tokens are refilled in requests per second.

```java
2. FixedWindowLimit(String limitId, Integer capacity, Duration period)
```
- **period**: Duration of each bucket/interval after which the counters get reset.

```java
3. SlidingLogLimit(String limitId, Integer capacity, Duration period)
```
- **period**: Here the period is a rolling window interval. Requests are counted between the current time and current time minus the period.

```java
4. SlidingWindowLimit(String limitId, Integer capacity, Duration period, Integer lookBackCount)
```
- **lookBackCount**: Numbers of buckets to sum over while checking to compare with capacity.

```java
5. LeakyBucketLimit(String limitId, Integer capacity, Integer rate)
```
- **capacity**: Capacity of the queue. Requests arriving after the queue is full are discarded.

- **Rate**: The rate at which the requests are processed per second.

## Usage

### Intialisation
After cloning the repository, you can build it into a JAR file and have it imported into your project. 

Then you would need to instantiate a JedisPool object with max connections that suits your requirements and pass it as an argument to the create rate limiter method.

```java
RateLimiterManager manager = new RateLimiterManager();
JedisPool jedisPool = new JedisPool(hostName, port);
RateLimiter rateLimiter = manager.createRateLimiter("rate_limiter", RateLimiterType.SLIDING_LOG, jedisPool);
```

### Adding Limits

Limits can be added at any time as we use CopyOnWriteArrayList to store limits array. To add a limit

```java
rateLimiter.addLimit(new SlidingLogLImit("limit_name", 5, Duration.ofSeconds(10))).addLimit(new SlidingLogLimit("limit_name2", 30, Duration.ofMinute(1)));
```

This adds limits of 5 requests per 10 seconds and 30 requests per minute to our rate limiter.

### Adding rate limiter to an API

Inside your method that handles the API call, you will have to add

```java
try {
    if(!(rateLimiter.tryRequest(identity, cost))){
        //return error response
    }
}
catch (RateLimiterException e){
    //handle error
}
```

tryRequest returns a boolean indicating whether the request should be let through or not. In the case of the leaky bucket, if the request is allowed then True is returned after waiting for the duration required by the algorithm. 

This method throws an exception if a limit does not exist, there's a limit type mismatch or for leaky bucket if there's more than one limit. This should be handled in the catch block and ideally, the request should be allowed if the rateLimiter throws an exception so your service isn't down.

For a demo Spring API, the code would look like this:

```java
@GetMapping("/")
public String index(@RequestHeader("identity") String identity) throws RateLimiterException {
    RateLimiter rateLimiter = manager.getRateLimiter("rate_limiter");
    if(!(rateLimiter.tryRequest(identity))){
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Calm down.");
    }
    return identity;
}
```


