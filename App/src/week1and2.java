import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long timestamp;

    Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

class TransactionAnalyzer {

    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public List<int[]> findTwoSum(int target) {
        List<int[]> result = new ArrayList<>();
        Map<Integer, Transaction> map = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    public List<int[]> findTwoSumWithinWindow(int target, long windowMillis) {
        List<int[]> result = new ArrayList<>();
        Map<Integer, List<Transaction>> map = new HashMap<>();

        transactions.sort(Comparator.comparingLong(t -> t.timestamp));

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction c : map.get(complement)) {
                    if (Math.abs(t.timestamp - c.timestamp) <= windowMillis) {
                        result.add(new int[]{c.id, t.id});
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    public List<List<Integer>> findKSum(int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        transactions.sort(Comparator.comparingInt(t -> t.amount));
        kSumHelper(transactions, 0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(List<Transaction> txns, int start, int k, int target,
                            List<Integer> path, List<List<Integer>> result) {
        if (k == 2) {
            int left = start, right = txns.size() - 1;
            while (left < right) {
                int sum = txns.get(left).amount + txns.get(right).amount;
                if (sum == target) {
                    List<Integer> pair = new ArrayList<>(path);
                    pair.add(txns.get(left).id);
                    pair.add(txns.get(right).id);
                    result.add(pair);
                    left++;
                    right--;
                } else if (sum < target) left++;
                else right--;
            }
            return;
        }

        for (int i = start; i < txns.size(); i++) {
            path.add(txns.get(i).id);
            kSumHelper(txns, i + 1, k - 1, target - txns.get(i).amount, path, result);
            path.remove(path.size() - 1);
        }
    }

    public List<Map<String, Object>> detectDuplicates() {
        List<Map<String, Object>> duplicates = new ArrayList<>();
        Map<String, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.amount + "|" + t.merchant;
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<String, List<Transaction>> entry : map.entrySet()) {
            List<Transaction> txns = entry.getValue();
            Set<String> accounts = new HashSet<>();
            for (Transaction t : txns) accounts.add(t.account);

            if (accounts.size() > 1) {
                Map<String, Object> dup = new HashMap<>();
                dup.put("amount", txns.get(0).amount);
                dup.put("merchant", txns.get(0).merchant);
                dup.put("accounts", accounts);
                duplicates.add(dup);
            }
        }
        return duplicates;
    }
}

public class week1and2 {
    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();

        analyzer.addTransaction(new Transaction(1, 500, "Store A", "acc1", 36000000));
        analyzer.addTransaction(new Transaction(2, 300, "Store B", "acc2", 36900000));
        analyzer.addTransaction(new Transaction(3, 200, "Store C", "acc3", 37800000));
        analyzer.addTransaction(new Transaction(4, 500, "Store A", "acc2", 38000000));

        System.out.println("findTwoSum(500) → " + analyzer.findTwoSum(500));
        System.out.println("findTwoSumWithinWindow(500, 3600000) → " +
                analyzer.findTwoSumWithinWindow(500, 3600000));
        System.out.println("findKSum(k=3, target=1000) → " + analyzer.findKSum(3, 1000));
        System.out.println("detectDuplicates() → " + analyzer.detectDuplicates());
    }
}