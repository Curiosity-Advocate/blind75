public class Leetcode_268 {
    
    public int missingNumber(int[] nums) {
        
        long sum = 0;
        for(int num : nums){
            sum += num;
        }

        return (int)((nums.length*(nums.length+1)/2) - sum);
    }
}
