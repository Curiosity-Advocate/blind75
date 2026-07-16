import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Leetcode_417 {

    public record Pair(int x, int y) {}

    int[][] graph;
    Set<Pair> visitedCells;
    public List<List<Integer>> pacificAtlantic(int[][] heights) {
        
        this.graph = heights;
        List<List<Integer>> ans = new ArrayList<>();
        
        this.visitedCells = new HashSet<>();

        // Explore from Atlantic Ocean
        for(int x = 0; x <heights.length; x++){
            climb(x,heights[0].length-1,this.graph[x][heights[0].length-1]);
        }
        for(int y = 0; y < heights[0].length; y++){
            climb(heights.length-1,y,this.graph[heights.length-1][y]);
        }

        //Explore from Pacific Ocean
        Set<Pair> seen = new HashSet<>();
        for(int x = 0; x <heights.length; x++){
            climb(x,0,this.graph[x][0],seen,ans);
        }
        for(int y = 0; y < heights[0].length; y++){
            climb(0,y,this.graph[0][y],seen,ans);
        }

        return ans;
    }

    private void climb(int x, int y, int prevHeight){

        if(x < 0 || y < 0 || x >= this.graph.length || y >= this.graph[0].length) return;

        if(this.graph[x][y] < prevHeight) return;
        if(!this.visitedCells.add(new Pair(x,y))) return;

        for(int dir : new int[]{-1,1}){
            climb(x+dir,y,this.graph[x][y]);
            climb(x,y+dir,this.graph[x][y]);
        }
    }

    private void climb(int x, int y, int prevHeight, Set<Pair> seen, List<List<Integer>> ans){

        if(x < 0 || y < 0 || x >= this.graph.length || y >= this.graph[0].length) return;

        if(this.graph[x][y] < prevHeight) return;

        Pair p = new Pair(x,y);
        if(!seen.add(p)) return;

        if(this.visitedCells.contains(p)){
            List<Integer> item = new ArrayList<>();
            item.add(p.x);
            item.add(p.y);
            ans.add(item);
        }

        for(int dir : new int[]{-1,1}){
            climb(x+dir,y,this.graph[x][y],seen,ans);
            climb(x,y+dir,this.graph[x][y],seen,ans);
        }
    }
}