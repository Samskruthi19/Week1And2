import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isWord = false;
    int frequency = 0;
}

class AutocompleteSystem {
    private TrieNode root = new TrieNode();

    public void addQuery(String query, int freq) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isWord = true;
        node.frequency += freq;
    }

    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }

        PriorityQueue<Map.Entry<String, Integer>> heap =
                new PriorityQueue<>((a, b) -> a.getValue() - b.getValue());

        dfs(node, new StringBuilder(prefix), heap);

        List<String> results = new ArrayList<>();
        while (!heap.isEmpty()) results.add(heap.poll().getKey());
        Collections.reverse(results);
        return results;
    }

    private void dfs(TrieNode node, StringBuilder sb,
                     PriorityQueue<Map.Entry<String, Integer>> heap) {
        if (node.isWord) {
            heap.offer(new AbstractMap.SimpleEntry<>(sb.toString(), node.frequency));
            if (heap.size() > 10) heap.poll();
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            sb.append(entry.getKey());
            dfs(entry.getValue(), sb, heap);
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    public void updateFrequency(String query) {
        addQuery(query, 1);
    }
}

public class week1and2 {
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        system.addQuery("java tutorial", 1234567);
        system.addQuery("javascript", 987654);
        system.addQuery("java download", 456789);
        system.addQuery("java 21 features", 10);
        system.addQuery("jav bus", 5);

        System.out.println("search(\"jav\") → ");
        List<String> suggestions = system.search("jav");
        int rank = 1;
        for (String s : suggestions) {
            System.out.println(rank + ". \"" + s + "\"");
            rank++;
        }

        System.out.println("\nUpdating frequency for \"java 21 features\"");
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("search(\"java 21\") → " + system.search("java 21"));
    }
}