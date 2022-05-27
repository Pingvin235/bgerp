package ru.bgcrm.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Обработчик шаблонов вида (${key}:value)(текст ${key1}:value1), скобки
 * выделяют необязательную область, подставляемую только если для ключа будет
 * определено значение.
 */
public class PatternFormatter {
    private static Map<String, Pattern> patterns = new ConcurrentHashMap<String, Pattern>(12);

    private static final String BEFORE_VAR = "\\(([\\wа-яА-Я\\,\\.\\s\\[\\]\\\\/#\\(\\)]*)\\$\\{";
    private static final String AFTER_VAR = "\\}([\\wа-яА-Я\\,\\.\\s\\[\\]\\\\/#\\(\\)]*)\\)";
    private static final Pattern VAR_PATTERN = Pattern.compile(BEFORE_VAR + "([\\w:]+)" + AFTER_VAR);

    /**
     * Выполняет в исходном шаблоне подстановку значений в указанные места. Если
     * значение пустое - то скобка просто удаляется из шаблона. Для корректной
     * обработки шаблона необходимо последовательно подставить в шаблон все
     * возможные ключи.
     *
     * @param pattern исходный шаблон.
     * @param key     ключ.
     * @param value   значение.
     * @return
     */
    public static String insertPatternPart(String pattern, String key, String value) {
        StringBuilder result = new StringBuilder(pattern.length());
        Pattern p = patterns.get(key);
        if (p == null) {
            p = Pattern.compile(BEFORE_VAR + key + AFTER_VAR);
            patterns.put(key, p);
        }
        Matcher m = p.matcher(pattern);
        if (m.find()) {
            if (Utils.notBlankString(value)) {
                result.append(pattern.substring(0, m.start()));
                String prefix = m.group(1);
                if (prefix.startsWith(",") && result.length() == 0) {
                    prefix = prefix.substring(1);
                }
                result.append(prefix);
                result.append(value);
                result.append(m.group(2));
                result.append(pattern.substring(m.end()));
            } else {
                result.append(pattern.substring(0, m.start()));
                result.append(pattern.substring(m.end()));
            }
        } else {
            result.append(pattern);
        }
        return result.toString();
    }

    public static String processPattern(String pattern, PatternItemProcessor processor) {
        StringBuilder result = new StringBuilder(pattern.length());

        int pointer = 0;

        Matcher m = VAR_PATTERN.matcher(pattern);
        while (m.find()) {
            result.append(pattern.substring(pointer, m.start()));

            String value = processor.processPatternItem(m.group(2));
            if (Utils.notBlankString(value)) {
                String prefix = m.group(1);
                if (prefix.startsWith(",") && result.length() == 0) {
                    prefix = prefix.substring(1);
                }
                result.append(prefix);
                result.append(value);
                result.append(m.group(3));
            }

            pointer = m.end();
        }

        result.append(pattern.substring(pointer));

        return result.toString();
    }

    public static interface PatternItemProcessor {
        public String processPatternItem(String variable);
    }
}