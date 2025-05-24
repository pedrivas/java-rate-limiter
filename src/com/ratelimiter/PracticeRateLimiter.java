package ratelimiter;

import java.util.*;
import java.util.concurrent.*;

public class PracticeRateLimiter {

    private final int maxRequests;
    private final int timeWindowMilliSeconds;
    private final ConcurrentHashMap<String, Deque<Long>> userTimestamps;

    public PracticeRateLimiter(int maxRequests, int timeWindowSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowMilliSeconds = timeWindowSeconds * 1000;
        this.userTimestamps = new ConcurrentHashMap<>();
    }

    public boolean allowRequest(String userId) {
        userTimestamps.putIfAbsent(userId, new ArrayDeque<>());
        long currentTimeMillis = System.currentTimeMillis();
        Deque<Long> timestamps = userTimestamps.get(userId);
        synchronized (timestamps){
            while (!timestamps.isEmpty() && currentTimeMillis - timestamps.peekFirst() > timeWindowMilliSeconds) {
                timestamps.pollFirst();
            }
            if (timestamps.size() < maxRequests) {
                timestamps.addLast(currentTimeMillis);
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        PracticeRateLimiter practiceRateLimiter = new PracticeRateLimiter(5, 3);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        final int threadCount = 100;
        final String userId = "user1";
        List<Future<Boolean>> results = new ArrayList<>();
        for(int i = 0; i< threadCount; i++){
            Future<Boolean> isAllowed = executorService.submit(() -> practiceRateLimiter.allowRequest(userId));
            results.add(isAllowed);
        }
        executorService.shutdown();
        executorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);

        int allowedCount = 0;
        int notAllowedCount = 0;

        for (Future<Boolean>result:results){
            if (result.get()){
                allowedCount++;
            } else {
                notAllowedCount++;
            }
        }

        System.out.println("Allowed" + allowedCount);
        System.out.println("Not Allowed" + notAllowedCount);
    }

}
