import java.util.HashSet;
import java.util.Set;

public class Leetcode_3 {
    
    public int lengthOfLongestSubstring(String s) {
        
        int max = 0;
        int left = 0;
        int right = 0;
        Set<Character> seen = new HashSet<>();

        for(char ch : s.toCharArray()){

            if(!seen.add(ch)){
                while(s.charAt(left) != ch){
                    seen.remove(s.charAt(left));
                    left++;
                }
                left++;
            }

            right++;

            max = Math.max(max,right-left);
        }

        return max;
    }
}
