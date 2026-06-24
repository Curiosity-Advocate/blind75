public class Leetcode_153 {
    
    public int findMin(int[] nums) {
        return  findMin(nums,0,nums.length-1);
    }

    private int findMin(int[] nums, int left, int right){

        if(left == right){
            return nums[left];
        }

        int mid = left + (right-left)/2;
        
        if(nums[mid] < nums[right]){
            return findMin(nums, left,  mid);
        }

        return findMin(nums, mid+1,  right);
    }
}
