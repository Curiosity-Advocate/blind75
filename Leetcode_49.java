import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Leetcode_49 {
    
    public List<List<String>> groupAnagrams(String[] strs) {
        
        List<List<String>> results = new ArrayList<>();

        Map<String,List<String>> anagramGraph = new HashMap<>();
        for(String str : strs){
            char[] chars = str.toCharArray();
            Arrays.sort(chars);
            String sortedStr = new String(chars);

            anagramGraph.putIfAbsent(sortedStr, new ArrayList<>());
            anagramGraph.get(sortedStr).add(str);
        }

        for(String key : anagramGraph.keySet()){
            results.add(anagramGraph.get(key));
        }

        return results;
    }
}
