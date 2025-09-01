package org.bgerp.util.text;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bgcrm.util.Utils;

/**
 * Processor for substitution patterns like <pre>(${key})(текст ${key1})}</pre>.
 * Braces bound not mandatory area, being added only when for the key inside has defined a value.
 *
 * @author Shamil Vakhitov
 */
public class PatternFormatter {
    private static final String BEFORE_VAR = "\\(([\\wа-яА-Я\\,\\.\\s\\[\\]\\\\/#\\(\\)]*)\\$\\{";
    private static final String AFTER_VAR = "\\}([\\wа-яА-Я\\,\\.\\s\\[\\]\\\\/#\\(\\)]*)\\)";
    private static final Pattern VAR_PATTERN = Pattern.compile(BEFORE_VAR + "([\\w:]+)" + AFTER_VAR);

    /**
     * Executes substitutions in a pattern.
     * @param pattern the pattern.
     * @param processor values provider for found variables, provided {@code null} values treated as empty strings.
     * @return pattern with applied substitutions.
     */
    public static String processPattern(String pattern, Function<String, String> processor) {
        StringBuilder result = new StringBuilder(pattern.length());

        int pos = 0;

        Matcher m = VAR_PATTERN.matcher(pattern);
        while (m.find()) {
            result.append(pattern.substring(pos, m.start()));

            String value = processor.apply(m.group(2));
            if (Utils.notBlankString(value)) {
                String prefix = m.group(1);
                if (prefix.startsWith(",") && result.length() == 0) {
                    prefix = prefix.substring(1);
                }
                result.append(prefix).append(value).append(m.group(3));
            }

            pos = m.end();
        }

        result.append(pattern.substring(pos));

        return result.toString();
    }

    /**
     * Executes substitutions in a pattern.
     * @param pattern the pattern.
     * @param values variable values map, provided {@code null} values treated as empty strings.
     * @return pattern with applied substitutions.
     */
    public static String processPattern(String pattern, Map<String, String> values) {
        return processPattern(pattern, key -> values.get(key));
    }
}