import javax.swing.tree.TreeNode;

public class Leetode_235 {
    
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        
        if(p.val > q.val) return lowestCommonAncestor(root,q,p);

        if(root.val == p.val) return p;

        if(root.val == q.val) return q;

        if(root.val > p.val && root.val < q.val) return root;

        if(root.val < p.val){
            return lowestCommonAncestor(root.right,p,q);
        }

        return lowestCommonAncestor(root.left,p,q);
    }

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode(int x) { val = x; }
    }
}
