public class Leetcode_11 {
    
    // Brute force
    /*
    public int maxArea(int[] height) {
        
        int max = Integer.MIN_VALUE;

        for(int i = 0; i < height.length; i++){
            for(int j = i+1; j < height.length; j++){
                int currVol = (j-i)*(Math.min(height[j],height[i]));
                if(currVol > max){
                    max = currVol;
                }
            }
        }

        return max;
    }*/
    public int maxArea(int[] height) {
        int left = 0;
        int right = height.length-1;

        int max = Integer.MIN_VALUE;

        while(left < right){
            int currVol = (right -left)*(Math.min(height[right],height[left]));
            if(currVol > max){
                max = currVol;
            }

            if(height[left] <= height[right]){
                left++;
            }
            else {
                right--;
            }

        }

        return max;
    }
}
