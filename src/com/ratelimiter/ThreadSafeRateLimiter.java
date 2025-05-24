package ratelimiter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;

public class ThreadSafeRateLimiter {

    private final int maxRequests;
    private final long timeWindowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> userRequestTimestamps;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        final ThreadSafeRateLimiter limiter = new ThreadSafeRateLimiter(10, 5);
        final String userId = "concurrentUser";
        final int threadCount = 50;

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            results.add(executor.submit(() -> limiter.allowRequest(userId)));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int allowed = 0;
        int denied = 0;

        for (Future<Boolean> result : results) {
            if (result.get()) {
                allowed++;
            } else {
                denied++;
            }
        }

        System.out.println("Allowed: " + allowed); // should be 10
        System.out.println("Denied: " + denied);   // should be 40
    }

    public ThreadSafeRateLimiter(int maxRequests, int timeWindowSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowSeconds * 1000L;
        this.userRequestTimestamps = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String userId) {
        long now = System.currentTimeMillis();

        // Create a new deque if user is new
        userRequestTimestamps.putIfAbsent(userId, new ArrayDeque<>());

        Deque<Long> timestamps = userRequestTimestamps.get(userId);

        synchronized (timestamps) {
            // Remove timestamps outside time window
            while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > timeWindowMillis) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            }
            return false;
        }
    }

}
