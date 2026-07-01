import java.util.HashMap;

public class Leetcode_300 {
    
    HashMap<Integer,Integer> memo;
    int max;
    public int lengthOfLIS(int[] nums) {
        
        this.memo = new HashMap<>();
        this.max = 1;
        for(int i = nums.length -1; i > -1;i--){
            if(this.memo.containsKey(nums[i])){
                this.max = Math.max(this.max,this.memo.get(i));
            }
            else {
                this.max = Math.max(this.max,lengthOfLIS(nums,i));
            }
        }
        return this.max;
    }

    private int lengthOfLIS(int[] nums, int index){

        int currMax = 1;
        for(int i = index-1; i > -1; i--){
            if(nums[index] > nums[i]){
                if(this.memo.containsKey(i)){
                    currMax = Math.max(currMax,1 + this.memo.get(i)); 
                }
                else {
                    currMax = Math.max(currMax,1 + lengthOfLIS(nums,i));
                }
            }
        }

        this.memo.put(index,currMax);

        return currMax;
    }
}
