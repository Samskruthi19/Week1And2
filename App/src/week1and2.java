import java.util.*;

class Event {
    String url;
    String userId;
    String source;

    Event(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class AnalyticsDashboard {

    private HashMap<String, Integer> pageViews = new HashMap<>();
    private HashMap<String, Set<String>> uniqueVisitors = new HashMap<>();
    private HashMap<String, Integer> sourceCount = new HashMap<>();

    public synchronized void processEvent(Event event) {
        pageViews.put(event.url, pageViews.getOrDefault(event.url, 0) + 1);

        uniqueVisitors.computeIfAbsent(event.url, k -> new HashSet<>()).add(event.userId);

        sourceCount.put(event.source, sourceCount.getOrDefault(event.source, 0) + 1);
    }

    public void startDashboard() {
        Thread dashboardThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    displayDashboard();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        dashboardThread.setDaemon(true);
        dashboardThread.start();
    }

    private synchronized void displayDashboard() {
        System.out.println("\n--- Dashboard Update ---");

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());

        pq.addAll(pageViews.entrySet());

        System.out.println("Top Pages:");
        int count = 0;
        while (!pq.isEmpty() && count < 10) {
            Map.Entry<String, Integer> entry = pq.poll();
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, new HashSet<>()).size();

            System.out.println((count + 1) + ". " + url + " - " + views + " views (" + unique + " unique)");
            count++;
        }

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : sourceCount.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
}

public class week1and2 {
    public static void main(String[] args) throws InterruptedException {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();
        dashboard.startDashboard();

        String[] urls = {"/article/breaking-news", "/sports/championship", "/tech/ai"};
        String[] sources = {"google", "facebook", "direct"};
        Random rand = new Random();

        for (int i = 0; i < 50; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String user = "user_" + rand.nextInt(20);
            String source = sources[rand.nextInt(sources.length)];

            dashboard.processEvent(new Event(url, user, source));
            Thread.sleep(100);
        }
    }
}