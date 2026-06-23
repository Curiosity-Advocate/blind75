import java.util.HashMap;

public class Leetcode_105 {
    
    HashMap<Integer,Integer> inOrderIndex;
    int[] preorder;
    int currPt;

    public TreeNode buildTree(int[] preorder, int[] inorder) {

        this.preorder = preorder;
        this.inOrderIndex = new HashMap<>();
        this.currPt = 0;

        for(int i = 0; i < inorder.length; i++){
            this.inOrderIndex.put(inorder[i],i);
        }

        return buildTree(0, preorder.length-1);
    }
    private TreeNode buildTree(int left, int right){

        if(left > right){
            return null;
        }

        TreeNode root = new TreeNode(this.preorder[this.currPt]);
        this.currPt++;
        int mid = this.inOrderIndex.get(root.val);

        root.left = buildTree(left, mid -1);
        root.right = buildTree(mid+1, right);

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
