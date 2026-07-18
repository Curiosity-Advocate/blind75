public class Leetcode_19 {
    
    public ListNode removeNthFromEnd(ListNode head, int n) {
        
        if(n == 1) return null;

        ListNode curr = new ListNode(-1);
        curr.next = head;
        ListNode nextNth = head;

        for(int i = 0; i < n+1; i++){
            nextNth = nextNth.next;
        }

        while(nextNth != null){
            curr = curr.next;
            nextNth = nextNth.next;
        }

        if(curr.val == -1){
            head = head.next;
        }
        else {
            curr.next = curr.next.next;
        }

        return head;
    }

    public class ListNode {
        int val;
        ListNode next;
        ListNode() {}
        ListNode(int val) { this.val = val; }
        ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    } 
}
