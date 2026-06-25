import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Leetcode_GraphValidTree {

    boolean[] seenVertices;
    Map<Integer,Set<Integer>> graph;
    public boolean validTree(int n, int[][] edges) {

        if(n == 1) return true;
        
        this.seenVertices = new boolean[n];
        this.graph = new HashMap<>();

        for(int[] edge : edges){
            this.graph.putIfAbsent(edge[0], new HashSet<Integer>());
            this.graph.putIfAbsent(edge[1], new HashSet<Integer>());
            
            this.graph.get(edge[0]).add(edge[1]);
            this.graph.get(edge[1]).add(edge[0]);
        }

        boolean hasCycle = hasCycle(0);
        if(hasCycle) return false;

        for(boolean visitedVertex : this.seenVertices){
            if(visitedVertex == false) return false;
        }

        return true;
    }

    private boolean hasCycle(int vertex){

        if(!this.graph.containsKey(vertex)) return false;

        if(this.seenVertices[vertex]) return true;

        this.seenVertices[vertex] = true;

        for(int endPoint : this.graph.get(vertex)){
            this.graph.get(endPoint).remove(vertex);
            if(hasCycle(endPoint)) return true;
        }
        return false;
    }
}