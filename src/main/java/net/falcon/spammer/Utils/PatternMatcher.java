package net.falcon.spammer.Utils;
import java.util.Stack;

public class PatternMatcher {

    // Method to evaluate a logical expression with nested parentheses
    public static int evaluateExpression(String expression) {
        Stack<Integer> values = new Stack<>(); // Stack to store boolean values (0 or 1)
        Stack<Character> operators = new Stack<>(); // Stack to store operators (&, |, !, ( )

        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);

            if (current == ' ') continue; // Skip spaces

            if (current == '0' || current == '1') {
                values.push(current - '0'); // Convert '0' or '1' to integer and push to values stack
            } else if (current == '(') {
                operators.push(current); // Push '(' to the operators stack
            } else if (current == ')') {
                // Process entire expression within the parentheses
                while (operators.peek() != '(') {
                    values.push(applyOperator(operators.pop(), values));
                }
                operators.pop(); // Pop the '(' after processing the expression
            } else if (current == '&' || current == '|' || current == '!') {
                // Handle precedence and apply operators if necessary
                while (!operators.isEmpty() && precedence(current) <= precedence(operators.peek())) {
                    values.push(applyOperator(operators.pop(), values));
                }
                operators.push(current); // Push the current operator to the stack
            }
        }

        // Apply remaining operators after parsing the entire expression
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values));
        }

        return values.pop(); // Final result
    }

    // Method to apply an operator to one or two values from the stack
    private static int applyOperator(char operator, Stack<Integer> values) {
        if (operator == '!') {
            // NOT is unary, apply it to the top of the stack
            int value = values.pop();
            return value == 0 ? 1 : 0;
        } else {
            // For binary operators (& and |), pop two values from the stack
            int b = values.pop();
            int a = values.pop();
            switch (operator) {
                case '&': return a & b;
                case '|': return a | b;
                default: throw new IllegalArgumentException("Invalid operator: " + operator);
            }
        }
    }

    // Method to determine operator precedence
    private static int precedence(char operator) {
        if (operator == '!') return 3; // NOT has the highest precedence
        if (operator == '&') return 2; // AND has the second-highest precedence
        if (operator == '|') return 1; // OR has the lowest precedence
        return 0;
    }

    // Method to parse substrings from the pattern and evaluate them against the main string
    public static String parseSubstrings(String mainStr, String expr) {
        String ops = "()|&!";
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);

            if (ops.contains(ch + "")) {
                res.append(ch);
            } else {
                int start = i;
                while (i < expr.length() && !ops.contains(expr.charAt(i) + "")) i++;
                String word = expr.substring(start, i);
                res.append( NameMatcher.containsSimilarName(mainStr, word) ? "1" : "0");
                i--; // Adjust position after loop
            }
        }

        return res.toString();
    }

    // Main method to evaluate pattern on the main string
    public static boolean evaluatePattern(String mainString, String pattern) {
        if(pattern.isEmpty()) return true; // Empty pattern matches everything
        // Step 1: Parse the pattern and convert words to 1 or 0 based on presence in mainString
        String parsedExpression = parseSubstrings(mainString, pattern);

        // Step 2: Evaluate the parsed logical expression
        return evaluateExpression(parsedExpression) == 1;
    }

    // Main method for testing
    public static void main(String[] args) {
        // Test cases with words in the main string and pattern
        String mainString = "hello world!";
        System.out.println(evaluatePattern(mainString, "hello&world"));
        System.out.println(evaluatePattern(mainString, "hello|!world"));
        System.out.println(evaluatePattern(mainString, "(hello|foo)&!"));
        System.out.println(evaluatePattern(mainString, "!(hello|foo)&world"));
    }
}
