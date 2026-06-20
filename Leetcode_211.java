public class Leetcode_211 {

    class Node {
        boolean isEnd;
        Node[] children = new Node[26];
    }

    class WordDictionary {

        Node root;

        public WordDictionary() {
            this.root = new Node();
        }

        public void addWord(String word) {

            Node currNode = this.root;
            for (char ch : word.toCharArray()) {
                int ind = ch - 'a';
                if (currNode.children[ind] == null) {
                    currNode.children[ind] = new Node();
                }
                currNode = currNode.children[ind];
            }

            currNode.isEnd = true;
        }

        public boolean search(String word) {
            return dfs(word.toCharArray(), 0, this.root);
        }

        private boolean dfs(char[] chars, int i, Node node) {

            if (node == null)
                return false;
            if (i == chars.length)
                return node.isEnd;

            char ch = chars[i];
            if (ch == '.') {
                for (Node child : node.children) {
                    if (child != null && dfs(chars, i + 1, child))
                        return true;
                }

                return false;
            }

            return dfs(chars, i + 1, node.children[ch - 'a']);
        }
    }
    /**
     * Your WordDictionary object will be instantiated and called as such:
     * WordDictionary obj = new WordDictionary();
     * obj.addWord(word);
     * boolean param_2 = obj.search(word);
     */
}
