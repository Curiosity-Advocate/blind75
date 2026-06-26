import java.util.HashMap;

public class Leetcode_213 {
    
    HashMap<Integer,Integer> memo;
    public int rob(int[] nums) {
        
        if(nums.length == 1) return nums[0];

        this.memo = new HashMap<>();
        int first = rob(nums,0,nums.length-2);

        this.memo = new HashMap<>();
        int second = rob(nums,1,nums.length-1);

        return Math.max(first,second);
    }

    private int rob(int[] nums, int minIndex, int index){

        if(index == (minIndex+0)){
            return nums[index];
        }
        if(index == (minIndex+1)){
            return Math.max(nums[minIndex],nums[index]);
        }
        if(index == (minIndex+2)){
            return Math.max(nums[minIndex+1],nums[minIndex]+nums[minIndex+2]);
        }

        if(!this.memo.containsKey(index-3)){
            this.memo.put(index-3,rob(nums,minIndex, index-3));
        }
        if(!this.memo.containsKey(index-2)){
            this.memo.put(index-2,rob(nums,minIndex,index-2));
        }

        return Math.max(this.memo.get(index-2) +nums[index], this.memo.get(index-3)+nums[index-1]);
    }
}
