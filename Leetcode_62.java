import java.util.HashMap;
import java.util.Map;

// You can use mathematical equation to find the answer with O(min(m,n)) time complexity and O(1) space complexity
public class Leetcode_62 {
    
    int maxRow;
    int maxCol;
    
    public int uniquePaths(int m, int n) {

        this.maxRow = m;
        this.maxCol = n;
        // It can replaced with int[][] to optimse constant factor. This can be solve with 1DP approach. The idea is to notices 
        // right and down direction constraint means knowing jth and (j-1)th row's count is enough to calculate the count
        Map<String,Integer> counterPerCell = new HashMap<>();

        return uniquePathCounter(0,0, counterPerCell);
    }

    private int uniquePathCounter(int m, int n,  Map<String,Integer> counterPerCell){

        if(m < 0 || m >= this.maxRow || n < 0 || n >= this.maxCol) return 0;

        if(m == this.maxRow-1 && n == this.maxCol-1) return 1;

        String key = String.valueOf(m)+'-'+String.valueOf(n);

        if(counterPerCell.containsKey(key)) return counterPerCell.get(key);

        int leftCount = uniquePathCounter(m+1,n,counterPerCell);
        int rightCount = uniquePathCounter(m,n+1,counterPerCell);

        if(!counterPerCell.containsKey(key)){
            counterPerCell.put(key,leftCount+rightCount);
        }

        return leftCount + rightCount;
    }
}
