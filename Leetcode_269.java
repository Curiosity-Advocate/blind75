import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Leetcode_269 {

    List<Character> results;
    HashMap<Character, HashSet<Character>> graph;
    HashMap<Character, Boolean> seen;

    public String foreignDictionary(String[] words) {

        // Initialise the map
        this.graph = new HashMap<>();
        for (int i = 0; i < words.length; i++) {
            for (char c : words[i].toCharArray()) {
                if (!this.graph.containsKey(c)) {
                    this.graph.put(c, new HashSet<>());
                }
            }
        }

        // Add edges
        for (int i = 0; i < words.length - 1; i++) {
            int minLen = Math.min(words[i].length(), words[i + 1].length());

            boolean edgeAdded = false;
            for (int j = 0; j < minLen; j++) {
                if (words[i].charAt(j) != words[i + 1].charAt(j)) {
                    this.graph.get(words[i].charAt(j)).add(words[i + 1].charAt(j));
                    edgeAdded = true;
                    break;
                }
            }

            if (!edgeAdded && words[i].length() > words[i + 1].length()) {
                return "";
            }
        }

        // Trace the graph
        this.seen = new HashMap<>();
        this.results = new ArrayList<>();
        for (char c : this.graph.keySet()) {
            if (hasCycleByDfs(c))
                return "";
        }

        // Construct the string
        Collections.reverse(this.results);
        StringBuilder sb = new StringBuilder();
        for (char c : this.results) {
            sb.append(c);
        }
        return sb.toString();
    }

    private boolean hasCycleByDfs(char c) {
        if (this.seen.containsKey(c))
            return this.seen.get(c);

        this.seen.put(c, true);

        for (char child : this.graph.get(c)) {
            if (hasCycleByDfs(child))
                return true;
        }

        this.seen.put(c, false);
        this.results.add(c);
        return false;
    }
}
