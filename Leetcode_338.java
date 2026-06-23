import java.util.Arrays;

public class Leetcode_338 {
    
    public int[] countBits(int n) {
        
        int[] ans = new int[n+1];
        Arrays.fill(ans,-1);

        for(int i = ans.length-1; i > -1; i--){

            int curr = ans[i];
            if(curr == -1){
                int counter = 0;
                int val = i;
                while(val > 0){
                    if(val %2 == 1){
                        counter++;
                    }
                    val /= 2;
                }
                ans[i] = counter;
                ans[i/2] = ans[i] - (i%2);
            }
        }

        return ans;
    }
    /*
    0 -> 10
    1 -> 11

    00 -> 100 --> 10
    01 -> 101 --> 10
    10 -> 110 --> 11
    11 -> 111 --> 11
    */
}
