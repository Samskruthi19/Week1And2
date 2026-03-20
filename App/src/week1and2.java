import java.util.*;

class InventoryManager {

    private HashMap<String, Integer> stockMap = new HashMap<>();
    private HashMap<String, LinkedHashMap<Integer, Integer>> waitingList = new HashMap<>();

    public void addProduct(String productId, int stock) {
        stockMap.put(productId, stock);
        waitingList.put(productId, new LinkedHashMap<>());
    }

    public String checkStock(String productId) {
        int stock = stockMap.getOrDefault(productId, 0);
        return stock + " units available";
    }

    public synchronized String purchaseItem(String productId, int userId) {
        int stock = stockMap.getOrDefault(productId, 0);

        if (stock > 0) {
            stockMap.put(productId, stock - 1);
            return "Success, " + (stock - 1) + " units remaining";
        } else {
            LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);
            int position = queue.size() + 1;
            queue.put(userId, position);
            return "Added to waiting list, position #" + position;
        }
    }
}

public class week1and2 {
    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();

        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println("checkStock(\"IPHONE15_256GB\") → " + manager.checkStock("IPHONE15_256GB"));

        System.out.println("purchaseItem(\"IPHONE15_256GB\", 12345) → " +
                manager.purchaseItem("IPHONE15_256GB", 12345));

        System.out.println("purchaseItem(\"IPHONE15_256GB\", 67890) → " +
                manager.purchaseItem("IPHONE15_256GB", 67890));

        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", 10000 + i);
        }

        System.out.println("purchaseItem(\"IPHONE15_256GB\", 99999) → " +
                manager.purchaseItem("IPHONE15_256GB", 99999));
    }
}