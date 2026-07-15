public class Leetcode_191 {
    
    public int hammingWeight(int n) {
        
        int result = 0;

        while(n != 0){

            result += n%2;

            n /= 2;
        }

        return result;
    }
}
