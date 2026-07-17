public class Leetcode_647 {
    
    String str;
    public int countSubstrings(String s) {
        
        this.str = s;

        int ans = 0;
        for(int i = 0; i < s.length(); i++){
            ans += (countPalindrome(i,i,0) + countPalindrome(i,i+1,0));
        }

        return ans;
    }

    private int countPalindrome(int left, int right, int counter){

        if(left < 0 || right > this.str.length()){
            return counter;
        }

        if(this.str.charAt(left) != this.str.charAt(right)) return counter;

        return countPalindrome(left-1,right+1, counter+1);
    }
}
