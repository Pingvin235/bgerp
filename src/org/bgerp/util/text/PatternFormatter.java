package org.bgerp.util.text;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bgcrm.util.Utils;

/**
 * Processor for substitution patterns like <pre>${key} text (text1 ${key1})}</pre>.
 * Braces bound not mandatory area, being added only when for the key inside has defined a value.
 *
 * @author Shamil Vakhitov
 */
public class PatternFormatter {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([\\w:]+)\\}");

    /**
     * Executes substitutions in a pattern
     * @param pattern the pattern
     * @param processor values provider for found variables, provided {@code null} values treated as empty strings
     * @return the pattern with applied substitutions
     */
    public static String processPattern(String pattern, Function<String, String> processor) {
        StringBuilder result = new StringBuilder(pattern.length());

        int pos = 0;

        final Matcher m = VAR_PATTERN.matcher(pattern);
        while (pos < pattern.length() && m.find(pos)) {
            final int start = m.start();
            int brace = pattern.indexOf('(', pos, start);
            final boolean braces = pos <= brace;

            result.append(pattern.substring(pos, braces ? brace : start));

            String name = m.group(1);
            String value = processor.apply(name);
            final boolean valueExists = Utils.notBlankString(value);

            if (valueExists) {
                if (braces)
                    result.append(pattern.substring(brace + 1, start));

                result.append(value);
            }

            final int end = m.end() - 1;
            if (braces) {
                brace = pattern.indexOf(')', end);
                if (end < brace) {
                    pos = brace + 1;
                    if (valueExists)
                        result.append(pattern.substring(end + 1, brace));
                } else
                    pos = end + 1;
            } else
                pos = end + 1;
        }

        if (pos < pattern.length())
            result.append(pattern.substring(pos));

        return result.toString();
    }

    /**
     * Executes substitutions in a pattern
     * @param pattern the pattern
     * @param values variable values map, provided {@code null} values treated as empty strings
     * @return the pattern with applied substitutions
     */
    public static String processPattern(String pattern, Map<String, String> values) {
        return processPattern(pattern, key -> values.get(key));
    }
}