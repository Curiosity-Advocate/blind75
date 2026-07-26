import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Leetcode_139 {
    
    private Map<Integer, Boolean> memo;
    private TrieNode root;

    public boolean wordBreak(String s, List<String> wordDict) {
        memo = new HashMap<>();
        root = new TrieNode();

        for (String word : wordDict) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                int charIdx = c - 'a';
                if (node.nodes[charIdx] == null) {
                    node.nodes[charIdx] = new TrieNode();
                }
                node = node.nodes[charIdx];
            }
            node.isWord = true;
        }

        return canBreak(s, 0);
    }

    private boolean canBreak(String s, int index) {
        if (index == s.length()) {
            return true;
        }

        if (memo.containsKey(index)) {
            return memo.get(index);
        }

        TrieNode node = root;

        for (int j = index; j < s.length(); j++) {
            int charIdx = s.charAt(j) - 'a';

            if (node.nodes[charIdx] == null) {
                break;
            }

            node = node.nodes[charIdx];

            if (node.isWord && canBreak(s, j + 1)) {
                memo.put(index, true);
                return true;
            }
        }

        memo.put(index, false);
        return false;
    }

    private static class TrieNode {
        TrieNode[] nodes = new TrieNode[26];
        boolean isWord = false;
    }
}