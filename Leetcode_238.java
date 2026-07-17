public class Leetcode_238 {
    
    // Initial solution
    public int[] productExceptSelf(int[] nums) {
        
        int[] leftProds = new int[nums.length];
        int[] rightProds = new int[nums.length];

        leftProds[0] = 1;
        rightProds[nums.length-1] = 1;

        for(int i = 0; i < nums.length-1; i++){
            leftProds[i+1] = leftProds[i] * nums[i];
        }

        for(int i = nums.length-1; i > 0; i--){
            rightProds[i-1] = leftProds[i] * nums[i];
        }

        int[] ans = new int[nums.length];
        for(int i = 0; i < nums.length; i++){
            ans[i] = leftProds[i] * rightProds[i];
        }

        return ans;
    }

    // space complexity of O(1)
    public int[] productExceptSelf(int[] nums){

        int[] ans = new int[nums.length];
        ans[0] = 1;

        for(int i = 1; i < nums.length; i++){
            ans[i] = ans[i-1] * nums[i-1];
        }

        int suffix = 1;
        for(int i = nums.length-1; i > -1; i--){
            ans[i] = ans[i] * suffix;
            suffix *= nums[i];

        }

        return ans;
    }
}
