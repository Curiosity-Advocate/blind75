import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

// There is optimal solution that uses bucket sort
public class Leetcode_347 {
    
    public int[] topKFrequent(int[] nums, int k) {
        
        Map<Integer,Integer> freqMap = new HashMap<>();
        for(int num : nums){
            freqMap.put(num, freqMap.getOrDefault(num, 0)+1);
        }

        PriorityQueue<Integer> topK = new PriorityQueue<>((item1,item2) -> freqMap.get(item1) - freqMap.get(item2));
        
        for(int key : freqMap.keySet()){
            if(topK.size() < k){
                topK.add(key);
            }

            else{
                if(freqMap.get(topK.peek()) < freqMap.get(key)){
                    topK.poll();
                    topK.add(key);
                }
            }
        }

        int[] results = new int[k];
        for(int i = 0; i < k; i++){
            results[i] = topK.poll();
        }

        return results;
    }

    public class ElementWithFreq {
        int val;
        int freq;

        public ElementWithFreq(int val, int freq){
            this.val = val;
            this.freq = freq;
        }        
    }
}
