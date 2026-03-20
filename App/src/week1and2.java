import java.util.*;

class Video {
    String id;
    String content;

    Video(String id, String content) {
        this.id = id;
        this.content = content;
    }
}

class MultiLevelCache {

    private final int L1_CAPACITY = 10000;
    private final int L2_CAPACITY = 100000;

    private LinkedHashMap<String, Video> L1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true);
    private HashMap<String, Video> L2Cache = new HashMap<>();
    private Map<String, Video> L3Database = new HashMap<>();
    private Map<String, Integer> accessCount = new HashMap<>();

    private int L1Hits = 0, L2Hits = 0, L3Hits = 0, totalRequests = 0;
    private double L1Time = 0, L2Time = 0, L3Time = 0;

    public void addVideoToDatabase(Video video) {
        L3Database.put(video.id, video);
    }

    public Video getVideo(String videoId) {
        totalRequests++;

        if (L1Cache.containsKey(videoId)) {
            L1Hits++;
            L1Time += 0.5;
            return L1Cache.get(videoId);
        }

        if (L2Cache.containsKey(videoId)) {
            L2Hits++;
            L2Time += 5;
            Video v = L2Cache.get(videoId);
            promoteToL1(videoId, v);
            return v;
        }

        if (L3Database.containsKey(videoId)) {
            L3Hits++;
            L3Time += 150;
            Video v = L3Database.get(videoId);
            accessCount.put(videoId, 1);
            addToL2(videoId, v);
            return v;
        }

        return null;
    }

    private void promoteToL1(String videoId, Video video) {
        if (L1Cache.size() >= L1_CAPACITY) {
            Iterator<String> it = L1Cache.keySet().iterator();
            it.next();
            it.remove();
        }
        L1Cache.put(videoId, video);
        accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);
    }

    private void addToL2(String videoId, Video video) {
        if (L2Cache.size() >= L2_CAPACITY) {
            Iterator<String> it = L2Cache.keySet().iterator();
            it.next();
            it.remove();
        }
        L2Cache.put(videoId, video);
    }

    public void updateVideo(String videoId, String newContent) {
        Video video = new Video(videoId, newContent);
        L3Database.put(videoId, video);
        L1Cache.remove(videoId);
        L2Cache.remove(videoId);
        accessCount.remove(videoId);
    }

    public void printStatistics() {
        double L1HitRate = totalRequests == 0 ? 0 : L1Hits * 100.0 / totalRequests;
        double L2HitRate = totalRequests == 0 ? 0 : L2Hits * 100.0 / totalRequests;
        double L3HitRate = totalRequests == 0 ? 0 : L3Hits * 100.0 / totalRequests;
        double overallHitRate = L1HitRate + L2HitRate + L3HitRate;

        double avgL1 = L1Hits == 0 ? 0 : L1Time / L1Hits;
        double avgL2 = L2Hits == 0 ? 0 : L2Time / L2Hits;
        double avgL3 = L3Hits == 0 ? 0 : L3Time / L3Hits;
        double avgOverall = totalRequests == 0 ? 0 : (L1Time + L2Time + L3Time) / totalRequests;

        System.out.printf("L1: Hit Rate %.1f%%, Avg Time: %.1fms%n", L1HitRate, avgL1);
        System.out.printf("L2: Hit Rate %.1f%%, Avg Time: %.1fms%n", L2HitRate, avgL2);
        System.out.printf("L3: Hit Rate %.1f%%, Avg Time: %.1fms%n", L3HitRate, avgL3);
        System.out.printf("Overall: Hit Rate %.1f%%, Avg Time: %.1fms%n", overallHitRate, avgOverall);
    }
}

public class week1and2 {
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        cache.addVideoToDatabase(new Video("video_123", "Video Content 123"));
        cache.addVideoToDatabase(new Video("video_999", "Video Content 999"));

        System.out.println("First access video_123 → " + cache.getVideo("video_123").content);
        System.out.println("Second access video_123 → " + cache.getVideo("video_123").content);
        System.out.println("Access video_999 → " + cache.getVideo("video_999").content);

        cache.printStatistics();

        cache.updateVideo("video_123", "Updated Content 123");
        System.out.println("After update video_123 → " + cache.getVideo("video_123").content);

        cache.printStatistics();
    }
}