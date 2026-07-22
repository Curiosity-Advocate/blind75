public class Leetcode_33 {
    
    // Initial solution
    /*
    int[] nums;
    public int search(int[] nums, int target) {
        this.nums = nums;
        
        int minIndex = 0;
        int maxIndex = nums.length-1;
        int midIndex = (nums.length-1)/2;

        while(minIndex < maxIndex){

            midIndex = minIndex + (maxIndex - minIndex)/2;

            if(nums[midIndex] > nums[maxIndex]){
                minIndex = midIndex+1;
            }
            else {
                maxIndex = midIndex;
            }
        }

        // minIndex or maxIndex is the index of the smallest element, aka pivot
        return find(0,nums.length-1, minIndex, target);
    }

    private int find(int min, int max, int offset, int target){

        if(min > max) return -1;

        int mid = min + (max-min)/2;
        int midWithOffset = getIndex(mid+offset);

        if(this.nums[midWithOffset] == target) return midWithOffset;

        if(this.nums[midWithOffset] < target){
            return find(mid+1, max, offset, target);
        }

        return find(min,mid-1,offset, target);
    }

    private int getIndex(int nextIndex){
        return (this.nums.length + nextIndex) % this.nums.length;
    }
    */

    // Optimal Solution
    public int search(int[] nums, int target){

        int left = 0;
        int right = nums.length-1;

        while(left <= right){

            int mid = left + (right - left)/2;

            if(nums[mid] == target) return mid;

            // left side is sorted
            if(nums[left] <= nums[mid]){

                if(nums[left] <= target && nums[mid] > target){
                    right = mid-1;
                }

                else {
                    left = mid+1;
                }
            }

            // right side is sorted
            else{
                if(nums[mid] < target && nums[right] >= target){
                    left = mid+1;
                }

                else {
                    right = mid-1;
                }
            }
        }

        return -1;
    }
}
