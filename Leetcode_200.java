public class Leetcode_200 {
    
    char[][] grid;
    public int numIslands(char[][] grid) {

        this.grid = grid;

        int numberOfIslands = 0;

        for(int x = 0; x < this.grid.length; x++){
            for(int y = 0; y < this.grid[x].length; y++){
                if(this.grid[x][y]=='1'){
                    DFS(x, y);
                    numberOfIslands++;
                }
            }
        }

        return numberOfIslands;
    }

    private void DFS(int x, int y){

        // Going beyond edges
        if(x < 0 || y < 0 || x >= this.grid.length || y >= this.grid[0].length) return;

        if(this.grid[x][y] == '0') return;

        this.grid[x][y] = '0';

        int[] neighbours = new int[]{-1,1};
        for(int neighbour : neighbours){
            DFS(x+neighbour,y);
            DFS(x,y+neighbour);
        }

    }
}
