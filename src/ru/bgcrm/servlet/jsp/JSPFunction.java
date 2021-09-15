package ru.bgcrm.servlet.jsp;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

/**
 * The functions called from util.tld library.
 */
public class JSPFunction {
    private static final Log log = Log.getLog();

    @Deprecated
    public static boolean contains(Object collection, Object object) {
        if (collection == null || object == null) {
            return false;
        }

        // проверка в массиве
        if (collection instanceof Object[]) {
            for (Object val : (Object[]) collection) {
                if (val.equals(object)) {
                    return true;
                }
            }
        }
        if (collection instanceof Collection<?>) {
            return ((Collection<?>) collection).contains(object);
        }
        if (collection instanceof Map<?, ?>) {
            return ((Map<?, ?>) collection).containsKey(object);
        }

        return collection.equals(object);
    }

    @Deprecated
    public static String concat(Object str1, Object str2) {
        return str1.toString() + str2.toString();
    }

    /**
     * Добавляет объект в колекцию. При этом удобно писать выражения вида <c:set var="v" value="${u:append(v,obj)}"/>
     * @param col
     * @param obj
     * @return
     */
    @Deprecated
    public static Object append(Object col, Object obj) {
        ArrayList<Object> newCol = null;
        if (col instanceof Collection<?>) {
            newCol = new ArrayList<Object>(((Collection<?>) col));
            newCol.add(obj);
        }

        return newCol;
    }

    /**
     * Возвращает строку если объект содержится в коллекции.
     * @param collection
     * @param object
     * @param string
     * @return
     */
    @Deprecated
    public static String string(Object collection, Object object, String string) {
        if (contains(collection, object)) {
            return string;
        }
        return "";
    }

    /**
     * Возвращает строку, если object истина.
     * @param object
     * @param string
     * @return
     */
    private static String string(Boolean object, String string) {
        if (object != null && object) {
            return string;
        }
        return "";
    }

    public static String checked(Object collection, Object object) {
        return string(collection, object, "checked='1'");
    }

    public static String checked(Boolean object) {
        return string(object, "checked='1'");
    }

    public static String selected(Object collection, Object object) {
        return string(collection, object, "selected='1'");
    }

    public static String selected(Boolean object) {
        return string(object, "selected='1'");
    }

    public static Integer getInt(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Utils.parseInt((String) value);
        }

        log.error("Incorrect object to int transformation: {}", value);

        return 0;
    }

    public static <T extends IdTitle> String getObjectTitles(List<T> fullList, Set<Integer> selectedIds) {
        return Utils.getObjectTitles(Utils.getObjectList(fullList, selectedIds));
    }

    public static final <T extends IdTitle> String getObjectTitles(Map<Integer, T> fullMap, List<Integer> selectedIds) {
        return Utils.getObjectTitles(Utils.getObjectList(fullMap, selectedIds));
    }

    public static <T extends IdTitle> List<T> getObjectList(List<T> fullList, Set<Integer> selectedIds) {
        return Utils.getObjectList(fullList, selectedIds);
    }

    public static final <T extends IdTitle> List<T> getObjectList(Map<Integer, T> fullMap, List<Integer> selectedIds) {
        return Utils.getObjectList(fullMap, selectedIds);
    }

    private static final String uidPrefix = "UIID";
    private static final AtomicLong uidGen = new AtomicLong(System.currentTimeMillis());

    public static String uiid() {
        return uidPrefix + uidGen.incrementAndGet();
    }

    public static String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    /**
     * Преобразует строку для отображения в виде HTML.
     * Символы '<' и '>', переносы строк.
     * @param value
     * @return
     */
    public static String htmlEncode(String value) {
        String result = value.replaceAll("<", "&lt;");
        result = result.replaceAll(">", "&gt;");
        result = result.replaceAll("\n", "<br/>");
        result = result.replaceAll("\t", "    ");

        return result;
    }

    private static final Pattern pattern = Pattern.compile("\\(?\\bhttps?://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");

    /**
     * Recognizes and replaces HTTP links to HTML code.
     * http://blog.codinghorror.com/the-problem-with-urls/
     * @param value
     * @return
     */
    public static String httpLinksToHtml(String value) {
        Matcher m = null;
        int pos = 0;

        boolean foundInAttr = false;

        while ((m = pattern.matcher(value)).find(pos)) {
            // URLs in attributes
            if (m.end() < value.length()) {
                var nextChar = value.charAt(m.end());
                if (nextChar == '"' || nextChar == '\'') {
                    pos = m.end();
                    foundInAttr = true;
                    continue;
                }
            }

            // URL in <a> body.
            if (foundInAttr) {
                pos = m.end();
                foundInAttr = false;
                continue;
            }

            final var url = m.group();
            final var link = "<a target=\"_blank\" href=\"" + url + "\">" + url + "</a>";
            value = value.substring(0, m.start()) +
                link +
                value.substring(m.end());

            pos = m.start() + link.length();
        }

        return value;
    }

    /**
     * Экранирует кавычки, используется для подготовки JS строк в JSP.
     * @param value
     * @return
     */
    public static String quotEscape(String value) {
        return value.replace("\"", "\\\"");
    }

    /**
     * Обрезает строку с HTML разметкой до максимальной длины, не разрывая теги.
     * Находится первая подходящая позиция после указанной длины.
     * @param s исходня строка.
     * @param limit максимальная длина.
     * @return
     */
    public static String truncateHtml(String s, Integer limit) {
        final int length = s.length();
        if (length < limit) {
            return s;
        }

        int okIndex = length - 1;
        boolean inClosingTag = false;
        int numOpenTags = 0;

        for (int i = 0; i < length; i++) {
            if (s.charAt(i) == '<') {
                if (s.charAt(i + 1) == '/') {
                    inClosingTag = true;
                } else {
                    numOpenTags++;
                }
            }
            if (s.charAt(i) == '>') {
                if (s.charAt(i - 1) == '/') {
                    numOpenTags--;
                }
                if (inClosingTag) {
                    numOpenTags--;
                    inClosingTag = false;
                }
            }

            if (numOpenTags == 0 && i >= limit) {
                okIndex = i;
                break;
            }
        }

        return s.substring(0, okIndex + 1);
    }

    public static Object getConfig(ParameterMap setup, String className) {
        return setup == null ? null : setup.getConfig(className);
    }

    public static Object newInstance(String className) throws Exception {
        return DynamicClassManager.newInstance(className);
    }

    public static String fileNameWithLastModTime(String path) {
        File file = new File("webapps" + path);
        return path + "?version=" + (file.lastModified() / 1000);
    }

    /** 
     * Возвращает значение ключа из Map а с персонализациями, значение по-умолчанию берётся из основной конфигурации.
     * @param user
     * @param key
     * @return
     */
    public static String getFromPers(User user, String key, String defaultValue) {
        return user.getPersonalizationMap().get(key, Setup.getSetup().get(key, defaultValue));
    }

}