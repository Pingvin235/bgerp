package ru.bgcrm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bgerp.util.Log;

public class RegexpStringUtils {
    private static final Log log = Log.getLog();

    /**
     * Ищет подстроки в строке соответствующие шаблону
     * @param text - исходная строка
     * @param regexpTemplate - шаблон
     * @return
     */
    public static List<String> findMatchesByTemplate(String text, String regexpTemplate) {
        Pattern pattern = Pattern.compile(regexpTemplate);
        Matcher matcher = pattern.matcher(text);

        List<String> variables = new ArrayList<String>();

        try {
            while (matcher.find()) {
                String match = matcher.group(0);
                variables.add(match);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return variables;
    }
}
