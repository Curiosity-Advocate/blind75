import java.util.HashMap;

public class Leetcode_1143 {
    
    record Pair(int i, int j) {}

    HashMap<Pair,Integer> memo;
    public int longestCommonSubsequence(String text1, String text2) {
        
        this.memo = new HashMap<>();
        return longestCommonSubsequence(text1,0,text2,0);
    }

    private int longestCommonSubsequence(String text1, int text1Index, String text2, int text2Index){

        if(text1Index >= text1.length() || text2Index >= text2.length()) return 0;

        if(text1.charAt(text1Index) == text2.charAt(text2Index)) {
            Pair key = new Pair(text1Index+1,text2Index+1);

            if(!this.memo.containsKey(key)){
                this.memo.put(key,longestCommonSubsequence(text1,text1Index+1,text2,text2Index+1));
            }
            return 1 + this.memo.get(key);
        }

        Pair key1 = new Pair(text1Index+1,text2Index);
        Pair key2 = new Pair(text1Index,text2Index+1);

        if(!this.memo.containsKey(key1)){
            this.memo.put(key1,longestCommonSubsequence(text1,text1Index+1,text2,text2Index));
        }

        if(!this.memo.containsKey(key2)){
            this.memo.put(key2,longestCommonSubsequence(text1,text1Index,text2,text2Index));
        }
        
        return Math.max(this.memo.get(key1),this.memo.get(key2));
    }
}
