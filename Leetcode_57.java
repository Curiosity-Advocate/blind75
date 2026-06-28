import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Leetcode_57 {
    
    public int[][] insert(int[][] intervals, int[] newInterval) {

        int[][] ans = new int[intervals.length+1][2];
        int i = 0;
        int actualIndex = 0;
        
        while(i < intervals.length && intervals[i][1] < newInterval[0]){
            ans[actualIndex++] = intervals[i++];
        }

        int left = newInterval[0];
        int right = newInterval[1];
        while(i < intervals.length && intervals[i][0] <= newInterval[1]){
            left = Math.min(intervals[i][0],left);
            right = Math.max(intervals[i][1], right);
            i++;
        }

        ans[actualIndex++] = new int[]{left,right};

        while(i < intervals.length && intervals[i][0] > newInterval[1]){
            ans[actualIndex++] = intervals[i++];
        }

        return Arrays.copyOf(ans,actualIndex);
    }
}
