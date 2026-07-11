import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Leetcode_56 {
    
    public int[][] merge(int[][] intervals) {

        Arrays.sort(intervals, (i1,i2)-> Integer.compare(i1[0], i2[0]));

        int min = intervals[0][0];
        int max = intervals[0][1];

        List<int[]> ans = new ArrayList<>();
        for(int[] interval : intervals){

            if(interval[0] > max){
                ans.add(new int[]{min,max});
                min = interval[0];
            }

            max = Math.max(max,interval[1]);
        }

        ans.add(new int[]{min,max});

        return ans.toArray(new int[0][]);
    }
}
