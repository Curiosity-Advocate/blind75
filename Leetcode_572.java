// There are more efficient algorithms that run in O(n+m) space and time. 
// The easy one is to assign each node a code. We concat left tree values to right tree plus the current node. 
// Then we do the same thing to the sub tree. The code for the root of sub-tree represents the tree. Then if any of the nodes in the main tree
// has the same code then we return true otherwise it'll be false
public class Leetcode_572 {
    
    public boolean isSubtree(TreeNode root, TreeNode subRoot) {
        
        if(hasSubTree(root, subRoot)) return true;
        
        if(root == null) return false;
        
        return isSubtree(root.left, subRoot) || isSubtree(root.right, subRoot);
    }

    private boolean hasSubTree(TreeNode root, TreeNode subRoot){
        
        if(root == null && subRoot == null) return true;

        if(root != null && subRoot != null && root.val == subRoot.val) {
            return hasSubTree(root.left, subRoot.left) && hasSubTree(root.right, subRoot.right);
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
