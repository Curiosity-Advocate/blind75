public class Leetcode_79 {
    
    String word;
    char[][] board;
    public boolean exist(char[][] board, String word) {
        this.word = word;
        this.board = board;

        for(int x = 0; x < board.length; x++){
            for(int y = 0; y < board[0].length; y++){
                if(dfs(x,y,0)) return true;
            }
        }

        return false;
    }

    private boolean dfs(int x, int y, int wordIndex){

        if(wordIndex == this.word.length()) return true;

        if(x < 0 || y < 0 || x >= this.board.length || y >= this.board[0].length) return false;

        if(this.board[x][y] != this.word.charAt(wordIndex)) return false;

        char temp = this.board[x][y];
        this.board[x][y] = '#';

        boolean ans = dfs(x+1,y,wordIndex+1) ||
        dfs(x-1,y,wordIndex+1) ||
        dfs(x,y+1,wordIndex+1) ||
        dfs(x,y-1,wordIndex+1);

        this.board[x][y] = temp;

        return ans;
    }
}
