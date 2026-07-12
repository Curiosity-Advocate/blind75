import java.util.List;
import java.util.PriorityQueue;

public class Leetcode_23 {
    
    /*
    public ListNode mergeKLists(ListNode[] lists) {

        if(lists.length == 0) return null;
        
        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((n1,n2) -> Integer.compare(n1.val, n2.val));
        for(ListNode chain : lists){
            while(chain != null){
                minHeap.add(chain);
                chain = chain.next;
            }            
        }

        if(minHeap.isEmpty()) return null;

        ListNode output = new ListNode(minHeap.poll().val);
        ListNode curr = output;

            while(!minHeap.isEmpty()){
                ListNode nextNode = minHeap.poll();
                curr.next = new ListNode(nextNode.val);
                curr = curr.next;
            }

        return output;
    }
    */

    public ListNode mergeKLists(ListNode[] lists) {

        PriorityQueue<ListNode> minHeap = new PriorityQueue<>((n1,n2) -> Integer.compare(n1.val, n2.val));

        for(int i = 0; i < lists.length; i++){
            if(lists[i] != null){
                minHeap.add(lists[i]);
            }
        }
        ListNode output = new ListNode(Integer.MIN_VALUE);
        ListNode curr = output;
        while(!minHeap.isEmpty()){
            ListNode minNode = minHeap.poll();
            curr.next = minNode;
            curr = curr.next;

            if(minNode.next != null){
                minHeap.add(minNode.next);
            }
        }

        return output.next;
    }

    public class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }
}
