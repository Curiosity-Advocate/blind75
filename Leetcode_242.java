public class Leetcode_242 {
    
    public boolean isAnagram(String s, String t) {
        
        if(s.length() != t.length()) return false;

        // Convert to Map<Character,Integer> to generalise
        int[] charFreq = new int[26];

        for(char sChar : s.toCharArray()){
            charFreq[sChar-'a']++;
        }

        for(char tChar : t.toCharArray()){
            charFreq[tChar-'a']--;
        }

        for(int i = 0; i < 26; i++){
            if(charFreq[i] != 0) return false;
        }

        return true;
    }
}
