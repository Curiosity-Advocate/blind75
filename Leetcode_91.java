import java.util.HashMap;
import java.util.Map;

public class Leetcode_91 {
    
    Map<Integer,Integer> memo;
    public int numDecodings(String s) {
        this.memo = new HashMap<>();
        return numDecodings(s.toCharArray(),0,0);
    }

    private int numDecodings(char[] chars, int index, int counter){

        if(index >= chars.length || chars[index]=='0'){
            return 0;
        }

        if(index == chars.length-1){
            counter++;
            return counter;
        }

        if(this.memo.containsKey(index)){
            return this.memo.get(index);
        }

        this.memo.put(index+1,numDecodings(chars,index+1,counter));

        if((chars[index] == '2' && chars[index+1]-'6' <= 0) || chars[index] == '1'){
            if(index+2 > chars.length -1) return (counter+1) + this.memo.getOrDefault(index+1,0); 
            this.memo.put(index+2,numDecodings(chars,index+2,counter));
        }

        return (this.memo.getOrDefault(index+1,0) + this.memo.getOrDefault(index+2,0));
    }
}
