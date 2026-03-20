import java.util.concurrent.*;

class TokenBucket {
    private int tokens;
    private final int maxTokens;
    private final double refillRate;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, double refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.nanoTime();
    }

    public synchronized boolean allowRequest() {
        refill();

        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    public synchronized int getRemainingTokens() {
        refill();
        return tokens;
    }

    public synchronized long getRetryAfterSeconds() {
        if (tokens > 0) return 0;
        double seconds = (1.0 / refillRate);
        return (long) Math.ceil(seconds);
    }

    private void refill() {
        long now = System.nanoTime();
        double tokensToAdd = (now - lastRefillTime) / 1_000_000_000.0 * refillRate;

        if (tokensToAdd > 0) {
            tokens = Math.min(maxTokens, tokens + (int) tokensToAdd);
            lastRefillTime = now;
        }
    }
}

class RateLimiter {
    private ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();
    private final int maxRequests = 1000;
    private final double refillRate = 1000.0 / 3600.0;

    public String checkRateLimit(String clientId) {
        TokenBucket bucket = clients.computeIfAbsent(clientId,
                k -> new TokenBucket(maxRequests, refillRate));

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry after " +
                    bucket.getRetryAfterSeconds() + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = clients.get(clientId);
        if (bucket == null) {
            return "{used: 0, limit: " + maxRequests + ", reset: 0}";
        }

        int remaining = bucket.getRemainingTokens();
        int used = maxRequests - remaining;

        long resetTime = System.currentTimeMillis() / 1000 + bucket.getRetryAfterSeconds();

        return "{used: " + used + ", limit: " + maxRequests + ", reset: " + resetTime + "}";
    }
}

public class week1and2 {
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();
        String clientId = "abc123";

        for (int i = 0; i < 5; i++) {
            System.out.println("checkRateLimit → " + limiter.checkRateLimit(clientId));
        }

        for (int i = 0; i < 1000; i++) {
            limiter.checkRateLimit(clientId);
        }

        System.out.println("checkRateLimit → " + limiter.checkRateLimit(clientId));

        System.out.println("getRateLimitStatus → " + limiter.getRateLimitStatus(clientId));
    }
}