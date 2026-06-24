import java.util.Collections;
import java.util.PriorityQueue;

public class Leetcode_295 {
    
    class MedianFinder{

        PriorityQueue<Integer> firstHalf;
        PriorityQueue<Integer> secondHalf;

        public MedianFinder() {
            this.firstHalf = new PriorityQueue<>(Collections.reverseOrder());
            this.secondHalf = new PriorityQueue<>();
        }
        
        public void addNum(int num) {
            
            if(this.firstHalf.isEmpty()){
                this.firstHalf.add(num);
            }
            else if(num <= this.firstHalf.peek()){
                this.firstHalf.add(num);

                if(this.firstHalf.size() > this.secondHalf.size()){
                    this.secondHalf.add(this.firstHalf.poll());
                }
            }

            else {
                this.secondHalf.add(num);

                if(this.secondHalf.size() > this.firstHalf.size()){
                    this.firstHalf.add(this.secondHalf.poll());
                }
            }
        }
        
        public double findMedian() {
            
            if(this.firstHalf.size() == this.secondHalf.size()){
                return (this.firstHalf.peek() + this.secondHalf.peek())/2.0;
            }

            if(this.firstHalf.size() > this.secondHalf.size()){
                return this.firstHalf.peek();
            }

            return this.secondHalf.peek();
        }
    }
}
