import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

// First Attempt
/*
class Leetcode_15 {
    public List<List<Integer>> threeSum(int[] nums) {

        List<List<Integer>> results = new ArrayList<>();
        int size = nums.length;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                for (int k = j + 1; k < size; k++) {
                    if (nums[i] + nums[j] + nums[k] == 0) {
                        List<Integer> ans = new ArrayList<>();
                        ans.add(nums[i]);
                        ans.add(nums[j]);
                        ans.add(nums[k]);

                        results.add(ans);
                    }
                }
            }
        }
        return results;
    }
}
*/

// My Solution
/*
class Leetcode_15 {
    public List<List<Integer>> threeSum(int[] nums) {

        List<List<Integer>> results = new ArrayList<>();
        HashMap<Integer, Integer> mapofFreq = new HashMap<>();

        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i++) {
            mapofFreq.put(nums[i], i);
        }

        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {

                int sum = nums[i] + nums[j];
                int lastIndex = mapofFreq.get(sum);

                if (lastIndex > j) {
                    List<Integer> ans = new ArrayList<>();
                    ans.add(nums[i]);
                    ans.add(nums[j]);
                    ans.add(-sum);

                    results.add(ans);
                }
                j = mapofFreq.get(nums[j]);
            }
            i = mapofFreq.get(nums[i]);
        }
        return results;
    }
}
*/

// My reconstruction of the Leetcode's optimal solution
class Leetcode_15 {
    public List<List<Integer>> threeSum(int[] nums) {

        List<List<Integer>> results = new ArrayList<>();

        Arrays.sort(nums);

        for (int i = 0; i < nums.length; i++) {

            if (i > 0 && nums[i] == nums[i - 1])
                continue;
            if (nums[i] > 0)
                break;

            int lowInd = i + 1;
            int hiInd = nums.length - 1;

            while (lowInd < hiInd) {

                int sum = nums[i] + nums[lowInd] + nums[hiInd];

                if (sum < 0) {
                    lowInd++;
                }

                else if (sum > 0) {
                    hiInd--;
                }

                else {
                    results.add(Arrays.asList(nums[i], nums[lowInd], nums[hiInd]));
                    lowInd++;
                    hiInd--;

                    while (lowInd < hiInd && nums[lowInd] == nums[lowInd - 1])
                        continue;
                    while (hiInd > lowInd && nums[hiInd] == nums[hiInd + 1])
                        continue;
                }
            }
        }

        return results;
    }

}