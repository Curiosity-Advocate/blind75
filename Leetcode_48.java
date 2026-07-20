import java.util.ArrayList;
import java.util.List;

public class Leetcode_48 {
    
    public void rotate(int[][] matrix) {
        rotate(matrix, 0);
    }

    private void rotate(int[][] matrix, int kth){

        if(kth >= matrix.length/2) return;

        for(int i = kth, last = matrix.length-1; i < last-kth; i++){

            int topLeft = matrix[kth][i];
            matrix[kth][i] = matrix[last-i][kth];
            matrix[last-i][kth] = matrix[last-kth][last-i];
            matrix[last-kth][last-i] = matrix[i][last-kth];
            matrix[i][last-kth] = topLeft;
        }

        rotate(matrix,kth+1);
    }
}
