import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Leetcode_253 {
    
    public int minMeetingRooms(List<Interval> intervals){
        
        Collections.sort(intervals, (i1,i2)->i1.start-i2.start);
        PriorityQueue<Integer> endTimes = new PriorityQueue<>();

        for(Interval interval : intervals){

            // If there is a room that ends before the new meeting, empty out of the room by polling it
            if(!endTimes.isEmpty() && endTimes.peek() <= interval.start){
                endTimes.poll();
            }
            endTimes.add(interval.end);
        }

        // Since we only poll if there is another meeting/interval that can occupy it.
        // This gives us the min # rooms
        return endTimes.size();
    }
    
    public class Interval {
        public int start, end;
        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
