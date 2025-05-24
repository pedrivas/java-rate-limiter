# 🔧 Mock Interview Problem: Design a Rate Limiter

## ✅ Problem Statement
Design a class RateLimiter that allows up to N requests per user per T seconds.

Implement the method:

java
Copiar
Editar
public boolean allowRequest(String userId)
If the user has made fewer than N requests in the last T seconds, return true and allow the request.

Otherwise, return false.

### 🎯 Requirements
Optimize for real-time request checking

Support multiple users concurrently

Avoid memory leaks over time

### 🧠 Clarifying Questions (ask aloud in the interview)
Some useful clarifications you might raise:

“Is time measured in seconds, milliseconds, or based on timestamps?”

“Can I assume single-threaded usage or should I make this thread-safe?”

“Can I use System.currentTimeMillis() to simulate the request times?”

### 💡 Constraints
You can assume N = 3, T = 10 seconds for simplicity, but it should be parameterizable.

You can use System.currentTimeMillis().

## Solution Walkthrough
### ✅ Step 1: Understand the Requirements
We want to allow at most N requests per T seconds per user.

Example:

If N = 3 and T = 10, a user can make 3 requests within any rolling 10-second window.

The 4th request in that 10s window must be denied.

This is a classic sliding window rate limiter.

### ✅ Step 2: Choose the Right Data Structure
We need to track request timestamps for each user.

We'll use:

java
Copiar
Editar
Map<String, Deque<Long>> userRequestTimestamps;
String → user ID

Deque<Long> → timestamps of recent requests (we’ll pop old ones)

### ✅ Step 3: Define the Class

```java

public class RateLimiter {
    private final int maxRequests;
    private final long timeWindowMillis;
    private final Map<String, Deque<Long>> userRequestTimestamps;

    public RateLimiter(int maxRequests, int timeWindowSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowSeconds * 1000L;
        this.userRequestTimestamps = new HashMap<>();
    }

    public boolean allowRequest(String userId) {
        long now = System.currentTimeMillis();
        userRequestTimestamps.putIfAbsent(userId, new ArrayDeque<>());
        Deque<Long> timestamps = userRequestTimestamps.get(userId);

        // Remove timestamps outside the window
        while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > timeWindowMillis) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        } else {
            return false;
        }
    }
}
```
### ✅ Step 4: Explanation
putIfAbsent: Initializes the deque if the user is new.

pollFirst: Removes old timestamps outside the T-second window.

If the size of the deque is under the max, the request is allowed and time is added.

### ✅ Step 5: Example Usage (Test in main)
```java

public static void main(String[] args) throws InterruptedException {
    RateLimiter limiter = new RateLimiter(3, 10); // 3 requests per 10 seconds
    String user = "user1";

    System.out.println(limiter.allowRequest(user)); // true
    System.out.println(limiter.allowRequest(user)); // true
    System.out.println(limiter.allowRequest(user)); // true
    System.out.println(limiter.allowRequest(user)); // false (4th in same window)

    Thread.sleep(11_000); // wait 11 seconds

    System.out.println(limiter.allowRequest(user)); // true (old ones expired)
}
```

## 🧠 Time and Space Complexity
Time: O(1) amortized per request (deque ops are fast)

Space: O(U × N), where U = unique users and N = max requests

### 🔄 Possible Follow-Ups in Interview
Prepare for these if they push further:

Make it thread-safe?

Use ConcurrentHashMap and Collections.synchronizedDeque() or a lock per user.

Global rate limiting (not per-user)?

Use a single shared deque for all users.

Evict inactive users?

Periodically clean up the map using ScheduledExecutorService or a time-based TTL cache.