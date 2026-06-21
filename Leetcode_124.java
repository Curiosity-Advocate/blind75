public class Leetcode_124 {

    int max = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        findMax(root);
        return this.max;
    }

    private int findMax(TreeNode root) {
        if (root == null)
            return 0;

        int leftSum = Math.max(findMax(root.left), 0);
        int rightSum = Math.max(findMax(root.right), 0);

        int currMax = Math.max(leftSum, rightSum);

        this.max = Math.max(this.max, leftSum + rightSum + root.val);

        return root.val + currMax;
    }

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode() {
        }

        TreeNode(int val) {
            this.val = val;
        }

        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }
}
