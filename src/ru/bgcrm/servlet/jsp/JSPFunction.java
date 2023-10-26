package ru.bgcrm.servlet.jsp;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bgerp.app.dist.inst.InstalledModule;
import org.bgerp.app.dist.inst.InstallerChanges;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

/**
 * Functions called from util.tld JSP library.
 *
 * @author Shamil Vakhitov
 */
public class JSPFunction {
    private static final Log log = Log.getLog();

    private static final AtomicLong UIID = new AtomicLong(System.currentTimeMillis());

    /**
     * Checks if {@link Collection}, {@link Map} or array from {@code collection} contains {@code object}.
     * @param collection may be {@link Collection}, {@link Map} or array.
     * @param object looked object
     * @return
     */
    private static boolean contains(Object collection, Object object) {
        if (collection == null || object == null) {
            return false;
        }

        if (collection instanceof Object[]) {
            for (Object val : (Object[]) collection) {
                if (val.equals(object)) {
                    return true;
                }
            }
        } else if (collection instanceof Collection<?>) {
            return ((Collection<?>) collection).contains(object);
        } else if (collection instanceof Map<?, ?>) {
            return ((Map<?, ?>) collection).containsKey(object);
        }

        return collection.equals(object);
    }

    /**
     * Gets string if {@link #contains(Object, Object)} call is true.
     * @param collection first param for contains.
     * @param object second param for contains.
     * @param string resulting string.
     * @return {@code string} or "".
     */
    private static String string(Object collection, Object object, String string) {
        if (contains(collection, object)) {
            return string;
        }
        return "";
    }

    /**
     * Gets string if {@code object} isn't null and {@code true}.
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

    /**
     * @see JSP function checkedFromCollection.
     */
    public static String checked(Object collection, Object object) {
        return string(collection, object, "checked='1'");
    }

    /**
     * @see JSP function checkedFromBool.
     */
    public static String checked(Boolean object) {
        return string(object, "checked='1'");
    }

    /**
     * @see JSP function selectedFromCollection.
     */
    public static String selected(Object collection, Object object) {
        return string(collection, object, "selected='1'");
    }

    /**
     * @see JSP function checkedFromBool.
     */
    public static String selected(Boolean object) {
        return string(object, "selected='1'");
    }

    /**
     * @see JSP function int.
     */
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

    /**
     * @return an unique identifier for HTML nodes.
     */
    public static String uiid() {
        return "UIID" + UIID.incrementAndGet();
    }

    public static String urlEncode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    /**
     * Replaces some HTML symbols.
     * <p>{@code <} to {@code &amp;lt;}
     * <p>{@code >} to {@code &amp;gt;}
     * <p>{@code \n} to {@code &lt;br/>}
     * <p>{@code \t} to 4 whitespaces
     * @param value input value.
     * @return string with replacements.
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

    public static String fileNameWithLastModTime(String path) {
        File file = new File("webapps" + path);
        return path + "?version=" + (file.lastModified() / 1000);
    }

    public static String docUrl(String url) {
        if (url.startsWith("http"))
            return url;

        final var m = InstalledModule.get(InstalledModule.MODULE_UPDATE);
        return
            m == null ?
            InstallerChanges.PRE_RELEASE_URL + "/doc/" + url :
            "https://bgerp.org/doc/" + m.getVersion() + "/manual/" + url;
    }
}