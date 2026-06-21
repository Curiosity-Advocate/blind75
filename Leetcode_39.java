import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Leetcode_39 {

    HashSet<String> seen;
    int[] candidates;
    List<List<Integer>> results;

    public List<List<Integer>> combinationSum(int[] candidates, int target) {

        this.seen = new HashSet<>();
        this.candidates = candidates;

        this.results = new ArrayList<>();
        combinationSum(0, new ArrayList<Integer>(), target);

        return this.results;
    }

    private void combinationSum(int startIndex, List<Integer> stack, int target) {

        if (target == 0) {
            this.results.add(new ArrayList<>(stack));
            return;
        }

        if(target < 0){
            return;
        }

        for (int i = startIndex; i < this.candidates.length; i++) {
            stack.add(this.candidates[i]);
            combinationSum(i, stack, target - this.candidates[i]);
            stack.removeLast();
        }

    }
}
