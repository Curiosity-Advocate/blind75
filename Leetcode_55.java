import java.util.HashMap;

public class Leetcode_55 {
    
    HashMap<Integer,Boolean> memo;
    public boolean canJump(int[] nums) {

        if(nums.length == 1) return true;

        this.memo = new HashMap<>();
        return canJump(nums, 0);
    }

    private boolean canJump(int[] nums, int index){

        if(index >= nums.length-1) return true;

        if(this.memo.containsKey(index)){
            return this.memo.get(index);
        }
        
        for(int i = 1; i <= nums[index]; i++){

            if(this.memo.get(index+i) == null){
                this.memo.put(index+i,canJump(nums,index+i));
            }

            if(this.memo.get(index+i)){
                return true;
            }
        }

        return false;
    }
}
