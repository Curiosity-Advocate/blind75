import org.omg.PortableInterceptor.NON_EXISTENT;

public class Leetcode_143 {

    public void reorderList(ListNode head) {

        int size = size(head);

        ListNode midPoint = head;
        for(int i = 0; i < size/2 -1; i++){
            midPoint = midPoint.next;
        }

        ListNode halfReversed = reverse(midPoint.next);
        midPoint.next = null;

        ListNode curr = head;
        ListNode currRev = halfReversed;
        ListNode nextCurr = null;
        ListNode nextRev = null;
        ListNode edgeOfWave = null;

        for(int i = 0; i < size/2; i++){
            // Keep track of next nodes
            nextCurr = curr.next;
            nextRev = currRev.next;
            
            // Weave both ends
            curr.next = currRev;
            currRev.next = nextCurr;
            
            // Start the new weave
            curr = nextCurr;
            edgeOfWave = currRev;
            currRev = nextRev;
        }

        if(size %2 == 1){
            edgeOfWave.next = currRev;
        }
    }

    private int size(ListNode head){

        int size = 0;
        ListNode curr = head;
        while(curr != null){
            curr = curr.next;
            size++;
        }

        return size;
    }
    
    private ListNode reverse(ListNode head){

        ListNode reversedList = head;
        ListNode prev = null;
        ListNode next = null;
        while(reversedList!= null){
            next = reversedList.next;
            reversedList.next = prev;
            prev = reversedList;
            reversedList = next;
        }
        return prev;
    }

    public class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }
}
