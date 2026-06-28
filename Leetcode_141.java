public class Leetcode_141 {
    
    public boolean hasCycle(ListNode head) {
        
        ListNode slowPt = head;
        ListNode fastPt = head;
        while(fastPt != null && fastPt.next != null){

            fastPt = fastPt.next.next;
            slowPt = slowPt.next;

            if(slowPt == fastPt) return true;
        }
        return false;
    }

    class ListNode {
      int val;
      ListNode next;
      ListNode(int x) {
          val = x;
          next = null;
      }
  }
}
