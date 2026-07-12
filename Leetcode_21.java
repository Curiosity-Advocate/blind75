public class Leetcode_21 {
    
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        
        ListNode minList1 = list1;
        ListNode minList2 = list2;

        ListNode output = new ListNode(Integer.MIN_VALUE);
        ListNode curr = output;

        while(minList1 != null || minList2 != null){
            
            if(minList1 != null){

                if(minList2 != null && minList1.val > minList2.val){
                    curr.next = minList2;
                    minList2 = minList2.next;
                }
                else {
                    curr.next = minList1;
                    minList1 = minList1.next;
                }
            }

            else if(minList2 != null){
                curr.next = minList2;
                minList2 = minList2.next;
            }

            curr = curr.next;
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
