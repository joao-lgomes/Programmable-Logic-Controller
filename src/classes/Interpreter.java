
package classes;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class Interpreter {
    public class IllegalExpression extends Exception {
        public IllegalExpression(String errorMessage) {
            super(errorMessage);
        }
    }
    
    public HashMap<String, String> expandExpression(HashMap<String, String> expressions) 
    {
        Pattern pattern = Pattern.compile("(.*)([B])\\d+(.*)");
        for(Map.Entry<String, String> entry : expressions.entrySet()) {
            while(pattern.matcher(entry.getValue()).matches()) {
                for(Map.Entry<String, String> entry2 : expressions.entrySet()) {
                    String a = "(" + entry2.getValue() + ")";
                    expressions.put(entry.getKey(), entry.getValue().replace(entry2.getKey(),a)); 
                }
            }
        }
        return expressions;
    }
    
    public HashMap<String, String> expandExpressionOut(HashMap<String, String> expressionsOut, HashMap<String, String> expressionsBoolean) 
    {
        Pattern pattern = Pattern.compile("(.*)([B])\\d+(.*)");
        for(Map.Entry<String, String> entry : expressionsOut.entrySet()) {
            while(pattern.matcher(entry.getValue()).matches()) {
                for(Map.Entry<String, String> entry2 : expressionsBoolean.entrySet()) {
                    String a = "(" + entry2.getValue() + ")";
                    expressionsOut.put(entry.getKey(), entry.getValue().replace(entry2.getKey(),a)); 
                }
            }
        }
        return expressionsOut;
    }

    public String[] validateExpressions(String[] lines) throws IllegalExpression {
        Pattern pattern = Pattern.compile("^([BS])\\d+=(.*)");
        String[] arrayString;
        HashMap<String,String> expressionsOut = new HashMap<String,String>();
        HashMap<String,String> expressionsBoolean = new HashMap<String,String>();
        int lineIndex = 1;
        for(String line: lines){
            if (!isValidVariable(line, pattern)){
                throw new IllegalExpression("Tipo de váriavel não reconhecida na linha " + lineIndex);
            }
            arrayString = line.trim().split("="); // POSITION 0: Variable or Output. Ex: S1, B2. POSITION 1: Expression. Ex: E1&&E2.
            if (!isValidExpression(arrayString[1])) {
                throw new IllegalExpression("Não foi possível reconhecer a expressão da linha" + lineIndex);
            }
            if (arrayString[0].startsWith("B")) {
                expressionsBoolean.put(arrayString[0], arrayString[1]);
            }
            else {
                expressionsOut.put(arrayString[0], arrayString[1]);
            }
            lineIndex++;
        }
        expressionsBoolean = expandExpression(expressionsBoolean);
        expressionsOut = expandExpressionOut(expressionsOut, expressionsBoolean);
        if(expressionsOut.size() != 8) {
            throw new IllegalExpression("Não foi definido 8 valores de saída");
        }
        String[] outputExpressions = new String[8];
        for(int i=0; i<8; i++){
            outputExpressions[i] = expressionsOut.get(String.format("S%d", i+1));
        }
        return outputExpressions;
    }
    
    static boolean isValidVariable(String line, Pattern pattern) {
        return pattern.matcher(line.trim()).matches();
    }
    
    static boolean isValidExpression(String expression) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine se = sem.getEngineByName("JavaScript");
        String replacedString = expression.replaceAll("([EB])\\d+", "1");
        try {
            se.eval(replacedString);
        } catch (Exception e) {
            return false; //INVALID EXPRESSION
        }
        return true;
    }
}
