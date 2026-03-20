import java.util.*;

class PlagiarismDetector {

    private int n;
    private HashMap<String, Set<String>> ngramIndex = new HashMap<>();
    private HashMap<String, List<String>> documentNgrams = new HashMap<>();

    public PlagiarismDetector(int n) {
        this.n = n;
    }

    public void addDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        documentNgrams.put(docId, ngrams);

        for (String gram : ngrams) {
            ngramIndex.computeIfAbsent(gram, k -> new HashSet<>()).add(docId);
        }
    }

    public void analyzeDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String gram : ngrams) {
            if (ngramIndex.containsKey(gram)) {
                for (String existingDoc : ngramIndex.get(gram)) {
                    matchCount.put(existingDoc, matchCount.getOrDefault(existingDoc, 0) + 1);
                }
            }
        }

        System.out.println("Extracted " + ngrams.size() + " n-grams");

        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();
            double similarity = (matches * 100.0) / ngrams.size();

            System.out.println("Found " + matches + " matching n-grams with \"" + otherDoc + "\"");
            System.out.printf("Similarity: %.1f%% ", similarity);

            if (similarity > 50) {
                System.out.println("(PLAGIARISM DETECTED)");
            } else if (similarity > 10) {
                System.out.println("(suspicious)");
            } else {
                System.out.println("(low)");
            }
        }
    }

    private List<String> generateNgrams(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }

        return ngrams;
    }
}

public class week1and2 {
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector(5);

        detector.addDocument("essay_089.txt",
                "this is a sample essay for plagiarism detection system testing purpose");

        detector.addDocument("essay_092.txt",
                "this is a sample essay for plagiarism detection system testing purpose with extra content added");

        detector.analyzeDocument("essay_123.txt",
                "this is a sample essay for plagiarism detection system testing purpose");
    }
}