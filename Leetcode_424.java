public class Leetcode_424 {
    
    public int characterReplacement(String s, int k) {
        
        int max = k;
        char[] chars = new char[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

        for(char ch : chars){

            int left = 0;
            int right = 0;

            int budget = k;
            while(right < s.length()){
                
                if(s.charAt(right) != ch){
                    budget--;
                }
                right++;

                if(budget < 0){
                    while(s.charAt(left) == ch){
                        left++;
                    }
                    left++;
                    budget++;
                }

                max = Math.max(max,right-left);
            }
        }

        return max;
    }
}
