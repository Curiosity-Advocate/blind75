import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

public class Leetcode_5 {
    
    // Initial Solution
    /*
    record Pair(int x, int y) {}

    Set<Pair> palindromeIntervalMaps;
    public String longestPalindrome(String s) {
        
        this.palindromeIntervalMaps = new HashSet<>();
        for(int i = 0; i < s.length(); i++){
            for(int j = i; j < s.length(); j++){
                if(isPalindrome(s,i,j)){
                    this.palindromeIntervalMaps.add(new Pair(i,j));
                }
            }
        }

        int max = -1;
        Pair maxPair = null;
        for(Pair pair : this.palindromeIntervalMaps){
            if(max < pair.y - pair.x){
                max = pair.y - pair.x;
                maxPair = pair;
            }
        }

        if(max == -1) return "";

        return s.substring(maxPair.x,maxPair.y+1);
    }

    private boolean isPalindrome(String s, Integer start, Integer end){

        if(start > end) return true;

        if(s.charAt(start) != s.charAt(end)) return false;

        return isPalindrome(s,start+1,end-1);
    }
    */

    public String longestPalindrome(String s){

        int start = 0;
        int end = 0;

        for(int i = 0; i < s.length(); i++){

            int len = findPalindromeLength(s,i,i);
            if(end - start < len){
                end = i + (len -1)/2;
                start = i - (len-1)/2;
            }

            len = findPalindromeLength(s,i,i+1);
            if(end - start < len){
                end = (i+1) + (len -1)/2;
                start = i - (len-1)/2;
            }
        }

        return s.substring(start,end+1);
    }

    private int findPalindromeLength(String s, int left, int right){

        while(left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)){
            left--;
            right++;
        }

        return right-left-1;
    }
}
