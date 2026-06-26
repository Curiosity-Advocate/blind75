public class Leetcode_208 {
    
    class TrieNode {

        TrieNode[] chars;
        boolean isEnd;

        public TrieNode(){
            this.chars = new TrieNode[26];
        }
    }
    class Trie {

        TrieNode root;
        public Trie() {
            this.root = new TrieNode();
        }
        
        public void insert(String word) {

            TrieNode currNode = this.root;
            for(char ch : word.toCharArray()){
                if(currNode.chars[ch-'a'] == null){
                   currNode.chars[ch-'a'] = new TrieNode();
                }
                currNode = currNode.chars[ch-'a'];
            }

            currNode.isEnd = true;
        }
        
        public boolean search(String word) {
            
            TrieNode currNode = this.root;
            for(char ch : word.toCharArray()){
                if(currNode.chars[ch-'a'] == null){
                   return false;
                }
                currNode = currNode.chars[ch-'a'];
            }

            return currNode.isEnd;
        }
        
        public boolean startsWith(String prefix) {
            
            TrieNode currNode = this.root;
            for(char ch : prefix.toCharArray()){
                if(currNode.chars[ch-'a'] == null){
                   return false;
                }
                currNode = currNode.chars[ch-'a'];
            }

            return true;
        }
    }
}
