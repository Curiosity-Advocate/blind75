public class Leetcode_226 {
    
    public TreeNode invertTree(TreeNode root) {
        
        if(root == null) return null;

        TreeNode leftTree = invertTree(root.right);
        root.right = invertTree(root.left);
        root.left = leftTree;
        
        return root;
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
