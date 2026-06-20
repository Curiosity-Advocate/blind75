public class Leetcode_121 {

    // Brute force
    /*
    public int maxProfit(int[] prices) {

        int max = 0;
        for (int i = 0; i < prices.length; i++) {
            for (int j = 0; j < prices.length; j++) {
                if (prices[j] - prices[i] > max) {
                    max = prices[j] - prices[i];
                }
            }
        }

        return max;
    }
    */

    public int maxProfit(int[] prices) {

        int max = 0;
        
        int buy = prices[0];
        int sell = prices[0];

        for(int i = 1; i < prices.length; i++){
            if(prices[i] > sell){
                sell = prices[i];
                int diff = sell - buy;
                if(diff > max){
                    max = diff;
                }
            }
            else if(prices[i] < buy){
                buy = prices[i];
                sell = buy;
            }
        }

        return max;
    }
}
