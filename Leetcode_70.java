public class Leetcode_70 {

    int counter = 0;

    public int climbStairs(int n) {

        countEachClimbPath(n);
        return this.counter;
    }

    private void countEachClimbPath(int n) {

        if (n == 0) {
            this.counter++;
        }

        climbStairs(n - 1);
        climbStairs(n - 2);
    }
}
