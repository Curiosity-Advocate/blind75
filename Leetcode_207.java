import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Leetcode_207 {
    
    HashMap<Integer,List<Integer>> graph;
    HashSet<Integer> cycleFreeVertices;
    HashSet<Integer> seen;
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        
        this.graph = new HashMap<>();
        this.seen = new HashSet<>();
        this.cycleFreeVertices = new HashSet<>();

        for(int[] req : prerequisites){
            this.graph.putIfAbsent(req[0], new ArrayList<Integer>());
            this.graph.get(req[0]).add(req[1]);
        }

        for(int course = 0; course < numCourses; course++){
            if(!this.seen.contains(course) && isCyclic(course)){
                return false;
            }
            this.seen.remove(course);
        }

        return true;
    }

    private boolean isCyclic(int course){

        if(this.cycleFreeVertices.contains(course)){
            return false;
        }
        
        if(!this.graph.containsKey(course)){
            return false;
        }

        if(!this.seen.add(course)){
            return true;
        }

        for(int prerequisite : this.graph.get(course)){
            if(isCyclic(prerequisite)) return true;
            this.seen.remove(prerequisite);
        }

        this.cycleFreeVertices.add(course);

        return false;
    }
}
