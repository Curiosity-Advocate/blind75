import java.util.Collections;
import java.util.List;

public class Leetcode_252 {
    
    public boolean canAttendMeetings(List<Interval> intervals) {
        
        Collections.sort(intervals, (i1,i2) -> Integer.compare(i1.start, i2.start));
        for(int i = 0; i < intervals.size()-1; i++){
            if(intervals.get(i).end > intervals.get(i+1).start) return false;
        }

        return true;
    }

    public class Interval {
        public int start, end;
        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
