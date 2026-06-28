public class Leetcode_230 {
    
    int rank;
    TreeNode kthNode;
    public int kthSmallest(TreeNode root, int k) {
       
        this.kthNode = new TreeNode(-1);
        this.rank = 0;
        
        dfs(root,k);
        return this.kthNode.val;
    }

    private void dfs(TreeNode root, int k){

        if(root == null) return;

        dfs(root.left, k);
        this.rank++;
        if(this.rank == k){
            this.kthNode = root;
            return;
        }
        dfs(root.right, k);
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
