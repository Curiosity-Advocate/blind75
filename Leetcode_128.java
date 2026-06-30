import java.util.HashMap;
import java.util.HashSet;

public class Leetcode_128{

    public int longestConsecutive(int[] nums) {
        
        if(nums.length <= 1) return nums.length;

        int counter = 0;
        
        HashSet<Integer> seen = new HashSet<>();
        for(int num : nums){
            seen.add(num);
        }

        int currentCounter = 0;
        int i = 0;
        for(int num : seen){

            int currentInt = num;
            if(!seen.contains(currentInt-1)){
                while(seen.contains(currentInt)){
                    currentCounter++;
                    currentInt++;
                }

                counter = Math.max(currentCounter,counter);
                currentCounter = 0;
            }
        }

        return counter;
    }
}