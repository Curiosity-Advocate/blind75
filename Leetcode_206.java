public class Leetcode_206 {
    
    // Recursive Solution with O(n) space
    /*
    public ListNode reverseList(ListNode head) {
        
        if(head == null) return null;

        return reverseList(null, head);
    }

    private ListNode reverseList(ListNode prev, ListNode curr){

        if(curr == null) return prev;

        ListNode next = curr.next;

        curr.next = prev;

        return reverseList(curr, next);
    }
    */

    // Iterative Solution with O(1) space
    public ListNode reverseList(ListNode head) {
        
        ListNode prev = null;
        ListNode curr = head;

        while(curr != null){
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
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
