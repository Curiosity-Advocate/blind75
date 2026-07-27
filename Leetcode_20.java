import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Leetcode_20 {
    
    public boolean isValid(String s) {
        
        Map<Character,Character> closingMatches = new HashMap<>();
        closingMatches.put('(',')');
        closingMatches.put('{','}');
        closingMatches.put('[',']');

        Deque<Character> openParentheses = new ArrayDeque<>();
        
        for(int i = 0; i < s.length(); i++){
            if(!closingMatches.containsKey(s.charAt(i))){
                if(openParentheses.isEmpty()) return false;

                if(closingMatches.get(openParentheses.peek()) != s.charAt(i)) return false;
                openParentheses.pop();
            }
            else {
                openParentheses.push(s.charAt(i));
            }
        }

        return openParentheses.isEmpty();
    }
}
