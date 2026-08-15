package org.bgerp.util.text;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Searches over dot-separated method calls in JEXL expressions.
 * Like {@code a.doSomething() or pp.val(4)}.
 *
 * @author Shamil Vakhitov
 */
public class CallsFinder {
    private final Pattern pattern;

    public CallsFinder(Set<String> variables, Set<String> calls) {
        var regexp = new StringBuilder();

        for (String name : variables) {
            regexp
                .append(regexp.length() == 0 ? "(" : "|")
                .append(name);
        }
        regexp.append(")\\.(");

        boolean first = true;
        for (String name : calls) {
            name = escape(name);

            if (first)
                first = false;
            else
                regexp.append("|");

            regexp.append(name);
        }
        regexp.append(")");

        pattern = Pattern.compile(regexp.toString(), Pattern.MULTILINE);
    }

    private String escape(String input) {
        var result = new StringBuilder(input.length() + 2);
        for (char c : input.toCharArray()) {
            if (c == '(' || c == ')') {
                result.append("\\");
            }
            result.append(c);
        }
        return result.toString();
    }

    public boolean find(String expression) {
        return pattern.matcher(expression).find();
    }
}
