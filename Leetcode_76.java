import java.util.HashMap;
import java.util.Map;

public class Leetcode_76 {

    public String minWindow(String s, String t) {

        Map<Character, Integer> counter = new HashMap<>();
        for (char ch : t.toCharArray()) {
            counter.put(ch, counter.getOrDefault(ch, 0) + 1);
        }

        int required = counter.size(); // number of distinct chars in t we must fully satisfy
        int formed = 0;                // number of distinct chars currently satisfied

        Map<Character, Integer> windowCounts = new HashMap<>();

        int start = 0;
        int minLen = Integer.MAX_VALUE;
        int[] ans = {0, -1}; // start, end (inclusive)

        int end = 0;
        for (end = 0; end < s.length(); end++) {
            char ch = s.charAt(end);
            windowCounts.put(ch, windowCounts.getOrDefault(ch, 0) + 1);

            if (counter.containsKey(ch) && windowCounts.get(ch).intValue() == counter.get(ch).intValue()) {
                formed++;
            }

            // Try to shrink the window as long as it's still valid
            while (start <= end && formed == required) {
                if (end - start + 1 < minLen) {
                    minLen = end - start + 1;
                    ans[0] = start;
                    ans[1] = end;
                }

                char leftChar = s.charAt(start);
                windowCounts.put(leftChar, windowCounts.get(leftChar) - 1);
                if (counter.containsKey(leftChar) && windowCounts.get(leftChar) < counter.get(leftChar)) {
                    formed--;
                }
                start++;
            }
        }

        return ans[1] == -1 ? "" : s.substring(ans[0], ans[1] + 1);
    }
}