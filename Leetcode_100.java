public class Leetcode_100 {
    
    public boolean isSameTree(TreeNode p, TreeNode q) {
        
        if((p != null && q != null && p.val == q.val)){
            return isSameTree(p.left,q.left) && isSameTree(p.right,q.right);
        }

        else if(p == q){
            return true;
        }

        return false;
    }

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode() {}
        TreeNode(int val) { this.val = val; }
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }
}
