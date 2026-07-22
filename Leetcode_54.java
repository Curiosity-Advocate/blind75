import java.util.ArrayList;
import java.util.List;

public class Leetcode_54 {
    
    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> order = new ArrayList<>();

        spiralOrder(matrix,order,0,matrix[0].length-1, 0, matrix.length-1);

        return order;
    }

    private void spiralOrder(int[][] matrix, List<Integer> order, int left, int right, int top, int bottom){

        if(left > right || top > bottom) return;

        addEdges(matrix,order, left, right, top, bottom);

        spiralOrder(matrix, order, left+1, right-1, top+1, bottom-1);
    }

    private void addEdges(int[][] matrix, List<Integer> order, int left, int right, int top, int bottom){
        
        for(int y = left; y <= right; y++){
            order.add(matrix[top][y]);
        }

        for(int x = top+1; x <= bottom; x++){
            order.add(matrix[x][right]);
        }

        // If top == bottom that means there is one row left which is covered by first loop
        if(top < bottom){
            for(int y = right-1; y >= left; y--){
                order.add(matrix[bottom][y]);
            }
        }

        // If left == right that means there is one column left which is covered by second loop
        if(left < right){
            for(int x = bottom-1; x > top; x--){
                order.add(matrix[x][left]);
            }
        }
    }
}
