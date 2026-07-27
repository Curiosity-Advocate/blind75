import java.util.HashMap;
import java.util.Map;

public class Leetcode_1 {
    
    public int[] twoSum(int[] nums, int target) {

        Map<Integer,Integer> complement = new HashMap<>();
        
        for(int i = 0; i < nums.length; i++){
            // The key idea to check before inserting is to avoid duplicates. So if the sum is 6 and we have [3,3] we are catching this 
            // without overriding anything. If we override that necessary means the duplicate value issue is not a concern anymore
            if(complement.containsKey(target-nums[i])){
               return new int[]{i,complement.get(target-nums[i])};
            }
            else {
                complement.put(nums[i],i);
            }
        }

        return new int[]{-1,-1};
    }
}
