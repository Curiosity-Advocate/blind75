import java.util.Arrays;

public class Leetcode_435 {
    
    public int eraseOverlapIntervals(int[][] intervals) {
        

        Arrays.sort(intervals, (i1,i2) -> Integer.compare(i1[1], i2[1]));

        int removalCounter = 0;
        int prevEnd = Integer.MIN_VALUE;

        for(int[] interval : intervals){

            if(interval[0] < prevEnd) removalCounter++;
            else prevEnd = interval[1];
            
        }

        return removalCounter;
    }
}
