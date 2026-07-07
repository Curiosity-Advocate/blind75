public class Leetcode_152 {
    
    // Brute force
    /*
    public int maxProduct(int[] nums) {
        
        int max = -11;
        for(int start = 0; start < nums.length; start++){
            int currProd = 1;
            for(int end = start; end < nums.length; end++){
                currProd *= nums[end];
                max = Math.max(max,currProd);
            }
        }
        return max;
    }
    */

    public int maxProduct(int[] nums) {

        int prevMin = 1;
        int prevMax = 1;
        int max = -11;

        for(int num : nums){

            int tempMin = prevMin*num;
            int tempMax = prevMax*num;
            
            prevMin = Math.min(num,Math.min(tempMin,tempMax));
            prevMax = Math.max(num,Math.max(tempMin,tempMax));

            max = Math.max(max,prevMax);
        }
        return max;
    }
}
