import java.util.ArrayList;
import java.util.List;

public class Leetcode_323 {
    
    List<Integer>[] graph;
    boolean[] visitedNodes;

    public int countComponents(int n, int[][] edges) {

        this.graph = new List[n];
        this.visitedNodes = new boolean[n];
        int numberOfConnectedComponents = 0;

        for(int[] edge : edges){
            if(graph[edge[0]] == null){
               graph[edge[0]] = new ArrayList<Integer>(); 
            }

            if(graph[edge[1]] == null){
               graph[edge[1]] = new ArrayList<Integer>(); 
            }

            graph[edge[0]].add(edge[1]);
            graph[edge[1]].add(edge[0]);
        }

        for(int i = 0; i < n; i++){
            if(!visitedNodes[i]){
                dfs(i);
                numberOfConnectedComponents++;
            }
        }

        return numberOfConnectedComponents;
    }

    private void dfs(int root){

        if(this.graph[root] == null){
            return;
        }

        for(int child : this.graph[root]){
            dfs(child);
        }

        this.visitedNodes[root] = true;
    }


    // Union Find Approach
    /*
    int[] parent;
    int[] rank;
    public int countComponents(int n, int[][] edges) {

        this.parent = new int[n];
        this.rank = new int[n];
        int components = n;

        for(int i = 0; i < n; i++){
            this.parent[i] = i;
        }

        for(int[] edge : edges){

            int root1 = find(edge[0]);
            int root2 = find(edge[1]);

            if(root1 == root2){
                union(root1,root2);
                components--;
            }
        }
        return components;
    }

    private int find(int i){
        
        if(this.parent[i] != i){
            this.parent[i] = find(parent[i]);
        }

        return parent[i];
    }

    private void union(int i, int j){

        if(this.rank[i] < this.rank[j]){
            this.parent[i] = j;
        }

        else if(this.rank[i] > this.rank[j]){
            this.parent[j] = i;
        }

        else {
            this.parent[i] = j;
            this.rank[j]++;
        }
    }
    */
}
