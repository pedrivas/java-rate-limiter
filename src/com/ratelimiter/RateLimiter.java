package ratelimiter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RateLimiter {

    private final Map<String, Deque<Long>> userTimestamps;
    private final long timeLimitInMilli;
    private final int requestLimit;

    public RateLimiter(long timeLimitInMilli, int requestLimit) {
        this.timeLimitInMilli = timeLimitInMilli;
        this.requestLimit = requestLimit;
        this.userTimestamps = new HashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {

        RateLimiter rateLimiter = new RateLimiter(5000, 3);
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));
        Thread.sleep(5000);
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));
        System.out.println(rateLimiter.allowRequest("a"));

    }

    public boolean allowRequest(String userId) {
        userTimestamps.putIfAbsent(userId, new ArrayDeque<>());
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = userTimestamps.get(userId);
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > timeLimitInMilli) {
            timestamps.pollFirst();
        }
        if (timestamps.size() < requestLimit) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }


}

