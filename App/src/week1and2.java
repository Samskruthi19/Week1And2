import java.util.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime;

    DNSEntry(String domain, String ipAddress, long ttlMillis) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + ttlMillis;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {

    private final int capacity;
    private LinkedHashMap<String, DNSEntry> cache;
    private int hits = 0;
    private int misses = 0;
    private long totalLookupTime = 0;
    private int totalRequests = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };
        startCleanupThread();
    }

    public synchronized String resolve(String domain) {
        long start = System.nanoTime();
        totalRequests++;

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            totalLookupTime += (System.nanoTime() - start);
            return "Cache HIT → " + entry.ipAddress;
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
        }

        misses++;
        String ip = queryUpstreamDNS(domain);
        cache.put(domain, new DNSEntry(domain, ip, 300000));

        totalLookupTime += (System.nanoTime() - start);
        return "Cache MISS → " + ip;
    }

    private String queryUpstreamDNS(String domain) {
        Random rand = new Random();
        return "172.217.14." + (100 + rand.nextInt(100));
    }

    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    cleanExpiredEntries();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleaner.setDaemon(true);
        cleaner.start();
    }

    private synchronized void cleanExpiredEntries() {
        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isExpired()) {
                it.remove();
            }
        }
    }

    public String getCacheStats() {
        double hitRate = totalRequests == 0 ? 0 : (hits * 100.0 / totalRequests);
        double avgTime = totalRequests == 0 ? 0 : (totalLookupTime / 1_000_000.0 / totalRequests);
        return "Hit Rate: " + String.format("%.2f", hitRate) + "%, Avg Lookup Time: " + String.format("%.2f", avgTime) + " ms";
    }
}

public class week1and2 {
    public static void main(String[] args) throws InterruptedException {
        DNSCache cache = new DNSCache(5);

        System.out.println("resolve(\"google.com\") → " + cache.resolve("google.com"));
        System.out.println("resolve(\"google.com\") → " + cache.resolve("google.com"));

        Thread.sleep(2000);

        System.out.println("resolve(\"google.com\") → " + cache.resolve("google.com"));

        Thread.sleep(310000);

        System.out.println("resolve(\"google.com\") → " + cache.resolve("google.com"));

        System.out.println("getCacheStats() → " + cache.getCacheStats());
    }
}