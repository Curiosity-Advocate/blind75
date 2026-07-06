public class Leetcode_104 {
    
    public int maxDepth(TreeNode root) {
        
        return maxDepth(root,0);
    }

    private int maxDepth(TreeNode root, int height){

        if(root == null){
            return height;
        }
        
        int leftMax = maxDepth(root.left, height);
        int rightMax = maxDepth(root.right,height);

        return Math.max(leftMax,rightMax)+1;
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
