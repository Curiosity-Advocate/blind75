import java.util.Arrays;
import java.util.HashMap;

public class Leetcode_322 {

    int[] coins;
    HashMap<Integer,Integer> map = new HashMap<>();

    public int coinChange(int[] coins, int amount) {

        Arrays.sort(coins);
        this.coins = coins;

        return coinChange(amount);
    }

    private int coinChange(int amount) {

        if (amount == 0)
            return 0;
        else if (amount < 0)
            return -1;

        if(this.map.containsKey(amount)) return this.map.get(amount);

        int ans = -1;
        for (int coin : coins) {
            
           int subCount = coinChange(amount - coin);
           if(subCount != -1){
                if(ans == -1 || (subCount + 1) < ans){
                    ans = subCount + 1;
                } 
           }
        }

        this.map.put(amount, ans);
        return ans;
    }
}
