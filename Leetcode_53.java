public class Leetcode_53 {
    
    // Initial solution
    /*
    int max;
    public int maxSubArray(int[] nums) {
        
        this.max = Integer.MIN_VALUE;
        maxSubArray(nums,nums.length-1);
        return this.max;
    }

    private int maxSubArray(int[] nums, int index){

        if(index == -1) return 0;

        int temp = Math.max(nums[index], nums[index] + maxSubArray(nums,index-1));
        this.max = Math.max(this.max, temp);

        return temp;
    }
    */
    public int maxSubArray(int[] nums){

        int sum = 0;
        int max = Integer.MIN_VALUE;

        for(int i = 0; i < nums.length; i++){

            sum = Math.max(nums[i], nums[i] + sum);
            max = Math.max(max,sum);
        }

        return max;
    }
}
