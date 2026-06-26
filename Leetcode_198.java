import java.util.HashMap;

public class Leetcode_198 {
    
    HashMap<Integer,Integer> memo;
    public int rob(int[] nums) {
        
        if(nums.length == 1){
            return nums[0];
        }
        if(nums.length == 2){
            return Math.max(nums[0],nums[1]);
        }
        if(nums.length == 3){
            return Math.max(nums[0]+nums[2], nums[1]);
        }

        this.memo = new HashMap<>();
        return rob(nums,nums.length-1);
    }

    private int rob(int[] nums, int index){

        if(index == 0){
            return nums[index];
        }
        if(index == 1){
            return Math.max(nums[0],nums[1]);
        }
        if(index == 2){
            return Math.max(nums[0]+nums[2], nums[1]);
        }
        
        if(!this.memo.containsKey(index-3)){
            this.memo.put(index-3, rob(nums,index-3));
        }

        if(!this.memo.containsKey(index-2)){
            this.memo.put(index-2, rob(nums,index-2));
        }
        
        return Math.max(this.memo.get(index-2) + nums[index], this.memo.get(index-3) + nums[index-1]);
    }
}
