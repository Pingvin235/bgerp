package ru.bgcrm.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.Id;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.ListItem;
import ru.bgcrm.model.Title;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterValuePair;

public class Utils {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Разделитель по-умолчанию: ", "
     */
    private static final String DEFAULT_DELIM = ", ";

    public static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    public static final char[] HEX_LOWERCASE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static final Logger log = Logger.getLogger(Utils.class);

    public static final String[] STRING_ARRAY = new String[0];
    public static final Integer[] INTEGER_ARRAY = new Integer[0];

    /**
     * Преобразует строку в int, в случае ошибки возращает 0.
     * @param str входная строка
     * @return число преобразованное из строки или 0 в случае ошибки
     */
    public static int parseInt(String str) {
        return parseInt(str, 0);
    }

    /**
     * Преобразует строку в int, в случае ошибки возращает defaultValue.
     * @param str входная строка
     * @param defaultValue значение возращаемое в случае ошибки при преобразовании
     * @return число преобразованное из строки или defaultValue в случае ошибки
     */
    public static int parseInt(String str, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt(str);
        } catch (Exception ex) {
        }
        return value;
    }

    /**
     * Преобразует строку в long, в случае ошибки возращает 0.
     * @param str входная строка
     * @return число преобразованное из строки или 0 в случае ошибки
     */
    public static long parseLong(String str) {
        return parseLong(str, 0L);
    }

    /**
     * Преобразует строку в long, в случае ошибки возращает defaultValue.
     * @param str входная строка
     * @param defaultValue значение возращаемое в случае ошибки при преобразовании
     * @return число преобразованное из строки или defaultValue в случае ошибки
     */
    public static long parseLong(String str, long defaultValue) {
        long value = defaultValue;
        try {
            value = Long.parseLong(str);
        } catch (Exception ex) {
        }
        return value;
    }

    /**
     * Преобразует строку в BigDecimal, и, в случае ошибки, возвращает значение
     * по умолчанию.
     * 
     * @param str строка представляющая BigDecimal-значение.
     * @param defaultValue BigDecimal-значение по умолчанию.
     * @return новый объект BigDecimal.
     */
    public static BigDecimal parseBigDecimal(final String str, final BigDecimal defaultValue) {
        try {
            return new BigDecimal(str);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Преобразует строку в BigDecimal, и, в случае ошибки, возвращает 0.0
     * 
     * @param str строка представляющая BigDecimal-значение.
     * @return новый объект BigDecimal.
     */
    public static BigDecimal parseBigDecimal(final String str) {
        return parseBigDecimal(str, new BigDecimal(0));
    }

    /**
     * Вызывает функию {@link #parseBoolean(String, boolean)} со вторым параметром false.
     * @param str
     * @return
     */
    public static boolean parseBoolean(String str) {
        return parseBoolean(str, false);
    }

    /**
     * Определяет хранится ли в строке число.
     * @param str
     * @return
     */
    public static boolean isStringNumber(String str) {
        for (int i = 0; i < str.length(); i++)
            if (Character.isLetter(str.charAt(i))) {
                return false;
            }
        return true;
    }

    /**
     * Преобразует строку в boolean.
     * @param str строка.
     * @param defaultValue значение по-умолчанию.
     * @return true - если str равно "1", "TRUE", "YES", "NO" без учёта регистра; false - если str равно "0", "FALSE", "NO" без учёта регистра; в ином случае - defaultValue. 
     */
    public static Boolean parseBoolean(String str, Boolean defaultValue) {
        Boolean result = defaultValue;
        try {
            String v = str.toUpperCase();
            if ("0".equals(v) || "FALSE".equals(v) || "NO".equals(v)) {
                result = false;
            } else if ("1".equals(v) || "TRUE".equals(v) || "YES".equals(v) || "ON".equals(v)) {
                result = true;
            }
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Преобразует boolean в строку "1" или "0". 
     * @param value
     * @return
     */
    public static final String booleanToStringInt(boolean value) {
        return value ? "1" : "0";
    }

    /**
     * Возвращает объект либо иное значение, если объект null.
     * @param value
     * @return
     */
    public static final <T> T maskNull(final T value, final T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Преобразует входную строку в пустую, если она null.
     * @param value
     * @return
     */
    public static final String maskNull(final String value) {
        return value == null ? "" : value;
    }

    /**
     * Возвращает входное значение тогда и только тогда, когда оно не равно null,
     * иначе возвращает BigDecimal.ZERO.
     * @param value
     * @return
     */
    public static final BigDecimal maskNullDecimal(final BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * Возвращает пустой немодифируемый Set, если value == null либо value.
     * @param <T>
     * @param value
     * @return
     */
    public static final <T> Set<T> maskNullSet(Set<T> value) {
        if (value == null) {
            value = Collections.emptySet();
        }
        return value;
    }

    /**
     * Возвращает null, если Set пустой либо null, иначе возвращает исходный Set.
     * @param value
     * @return
     */
    public static final <T> Set<T> emptyToNull(Set<T> value) {
        if (value == null || value.size() == 0) {
            return null;
        }
        return value;
    }

    /**
     * Возвращает null, если List пустой либо null, иначе возвращает исходный List.
     * @param value
     * @return
     */
    public static final <T> List<T> emptyToNull(List<T> value) {
        if (value == null || value.size() == 0) {
            return null;
        }
        return value;
    }

    /**
     * Преобразует входную строку в значение по-умолчанию, если она null или пустая.
     * @param value
     * @return
     */
    public static final String maskEmpty(final String value, final String defaultValue) {
        return isEmptyString(value) ? defaultValue : value;
    }

    /**
     * Проверяет, является ли строка пустой или null.
     * @param value проверяемая строка
     * @return true - если пустая или null
     */
    public static final boolean isEmptyString(String value) {
        return value == null || value.length() == 0;
    }

    /**
     * Проверяет, является ли строка пустой, состоящей из пробельных символов или null.
     * @param value проверяемая строка
     * @return true если пустая, состоит из пробельных символов или null
     */
    public static final boolean isBlankString(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Проверяет, является ли строка пустой или null.
     * @param value проверяемая строка
     * @return false если пустая или null
     */
    public static final boolean notEmptyString(String value) {
        return value != null && value.length() > 0;
    }

    /**
     * Проверяет, является ли строка пустой, состоящей из пробельных символов или null.
     * @param value проверяемая строка
     * @return false - если пустая, состоит из пробельных символов или null
     */
    public static final boolean notBlankString(String value) {
        return value != null && value.trim().length() > 0;
    }

    /**
     * Вызывается {@link #toString(Collection, String, String)} с
     * параметром emptyValue="", delim={@link #DEFAULT_DELIM}.
     * @param valuesList
     * @return
     */
    public static final String toString(Collection<?> valuesList) {
        return toString(valuesList, "", DEFAULT_DELIM);
    }

    /**
     * Преобразовывает коллекцию в строку, разделенную значениями delim.
     * @param valuesList коллектиция
     * @param emptyValue значение при пустом списке
     * @param delim разделитель
     * @return
     */
    public static final String toString(Collection<?> valuesList, String emptyValue, String delim) {
        if (valuesList != null && valuesList.size() != 0) {
            StringBuilder result = new StringBuilder(valuesList.size() * 5);
            for (Object next : valuesList) {
                addObjectToList(result, next, delim);
            }
            return result.toString();
        }
        return emptyValue;
    }

    private static void addObjectToList(StringBuilder result, Object next, String delim) {
        if (result.length() != 0) {
            result.append(delim);
        }
        if (next instanceof Integer) {
            result.append(((Integer) next).intValue());
        } else {
            result.append(next);
        }
    }

    /**
     * Преобразует строку с разделителями - запятыми или точками с запятой к списку Integer.
     * @param valuesStr
     * @return
     */
    public static final List<Integer> toIntegerList(String valuesStr) {
        return toIntegerList(valuesStr, ",;");
    }

    /**
     * Преобразует строку с произвольными разделителями - символами в delims в список Integer.
     * @param valuesStr
     * @param delims
     * @return
     */
    public static final List<Integer> toIntegerList(String valuesStr, String delims) {
        List<Integer> result = new ArrayList<Integer>();

        if (notBlankString(valuesStr)) {
            StringTokenizer st = new StringTokenizer(valuesStr.trim(), delims);
            while (st.hasMoreTokens()) {
                try {
                    result.add(Integer.valueOf(st.nextToken().trim()));
                } catch (Exception e) {}
            }
        }

        return result;
    }

    /**
     * Преобразование строки, разделенной запятыми к набору Integer.
     * @param valuesStr
     * @return
     */
    public static final Set<Integer> toIntegerSet(String valuesStr) {
        Set<Integer> result = new LinkedHashSet<Integer>();

        if (notBlankString(valuesStr)) {
            StringTokenizer st = new StringTokenizer(valuesStr.trim(), ",;");
            while (st.hasMoreTokens()) {
                try {
                    result.add(Integer.valueOf(st.nextToken().trim()));
                } catch (Exception e) {}
            }
        }

        return result;
    }

    /**
     * Преобразует строку разделённую произвольными разделителями - симвоолами в delims в набор строк.
     * @param valuesStr
     * @param delims
     * @return
     */
    public static final Set<String> toSet(String valuesStr, String delims) {
        Set<String> result = new LinkedHashSet<String>();
        if (notBlankString(valuesStr)) {
            StringTokenizer st = new StringTokenizer(valuesStr.trim(), delims);
            while (st.hasMoreTokens()) {
                result.add(st.nextToken());
            }
        }
        return result;
    }

    /**
     * Преобразует строку разделённую , либо ; в набор строк.
     * @param valuesStr
     * @return
     */
    public static final Set<String> toSet(String valuesStr) {
        Set<String> result = new HashSet<String>();
        if (notBlankString(valuesStr)) {
            StringTokenizer st = new StringTokenizer(valuesStr.trim(), ",;");
            while (st.hasMoreTokens()) {
                result.add(st.nextToken().trim());
            }
        }
        return result;
    }

    /**
     * Преобразует строку разделённую , либо ; в список строк.
     * @param valuesStr
     * @return
     */
    public static final List<String> toList(String valuesStr) {
        return toList(valuesStr, ",;");
    }

    /**
     * Преобразует строку с произвольными разделителями - симвоолами в delims в список строк.
     * @param valuesStr
     * @param delims
     * @return
     */
    public static final List<String> toList(String valuesStr, String delims) {
        List<String> result = new ArrayList<String>();

        if (notBlankString(valuesStr)) {
            StringTokenizer st = new StringTokenizer(valuesStr.trim(), delims);
            while (st.hasMoreTokens()) {
                result.add(st.nextToken().trim());
            }
        }

        return result;
    }

    /**
     * Преобразует лист строк в planeText с произвольным разделителем
     * @param config
     * @param separator
     * @return
     */
    public static final String toText(List<String> config, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object configLine : config.toArray()) {
            sb.append(configLine).append(separator);
        }
        return sb.toString();
    }

    /**
     * Преобразует несколько элементов в набор.
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public static final <T> Set<T> toSet(T... value) {
        return new HashSet<T>(Arrays.asList(value));
    }

    /**
     * Преобразует несколько элементов в список.
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public static final <T> List<T> toList(T... value) {
        return Arrays.asList(value);
    }

    /**
     * Возвращает список кодов объектов.
     * @param list
     * @return
     */
    public static final <T extends IdTitle> List<Integer> getObjectIdsList(Collection<T> list) {
        List<Integer> result = new ArrayList<Integer>(list.size());
        for (T object : list) {
            result.add(object.getId());
        }
        return result;
    }

    /**
     * Возаращает набор кодов объектов.
     * @param list
     * @return
     */
    public static final <T extends IdTitle> Set<Integer> getObjectIdsSet(Collection<T> list) {
        Set<Integer> result = new HashSet<Integer>(list.size());
        for (T object : list) {
            result.add(object.getId());
        }
        return result;
    }

    /**
     * Возвращает коды объектов из коллекции через запятую.
     * @param list
     * @return
     */
    public static final <T extends Id> String getObjectIds(Collection<T> list) {
        return getObjectIds(list, null);
    }

    /**
     * Возвращает коды объектов из коллекции через запятую с указанным началом строки.
     * @param list
     * @param startValues начало строки.
     * @return
     */
    public static final <T extends Id> String getObjectIds(Collection<T> list, String startValues) {
        return getObjectIds(list, startValues, DEFAULT_DELIM);
    }

    /**
     * Возвращает коды объектов из коллекции с указанием разделителя и начала строки.
     * @param list 
     * @param startValues начало строки.
     * @param delim разделитель кодов.
     * @return
     */
    public static <T extends Id> String getObjectIds(Collection<T> list, String startValues, String delim) {
        StringBuilder result = new StringBuilder();
        if (notEmptyString(startValues)) {
            result.append(startValues);
        }

        if (list != null) {
            for (T object : list) {
                if (result.length() != 0) {
                    result.append(delim);
                }
                result.append(object.getId());
            }
        }

        return result.toString();
    }

    /**
     * Возвращает наименования объектов через запятую.
     * @param list
     * @return
     */
    public static final <T extends Title> String getObjectTitles(Collection<T> list) {
        return getObjectTitles(list, null);
    }

    /**
     * Возвращает наименования объектов с указанным началом строки.
     * @param list
     * @param startValues начало строки.
     * @return
     */
    public static final <T extends Title> String getObjectTitles(Collection<T> list, String startValues) {
        return getObjectTitles(list, startValues, DEFAULT_DELIM);
    }

    /**
     * Возвращает наименования объектов с указанием начала строки и разделителя.
     * @param list
     * @param startValues начало строки.
     * @param delim разделитель наименований объектов.
     * @return
     */
    public static <T extends Title> String getObjectTitles(Collection<T> list, String startValues, String delim) {
        StringBuilder result = new StringBuilder();
        if (notEmptyString(startValues)) {
            result.append(startValues);
        }

        if (list != null) {
            for (T object : list) {
                if (result.length() != 0) {
                    result.append(delim);
                }
                result.append(object.getTitle());
            }
        }

        return result.toString();
    }

    /**
     * Возвращает объекты из полного списка с указанными кодами.
     * @param fullList полный список объектов.
     * @param selectedIds коды.
     * @return
     */
    public static final <T extends IdTitle> List<T> getObjectList(List<T> fullList, Set<Integer> selectedIds) {
        List<T> result = new ArrayList<T>();

        for (T object : fullList) {
            if (selectedIds != null && selectedIds.contains(object.getId())) {
                result.add(object);
            }
        }

        return result;
    }

    /**
     * Возвращает объекты из полного мапа с указанным порядком кодов.
     * @param fullMap полный мап объектов.
     * @param selectedIds требуемый порядок кодов.
     * @return
     */
    public static final <T extends IdTitle> List<T> getObjectList(Map<Integer, T> fullMap, List<Integer> selectedIds) {
        List<T> result = new ArrayList<T>();

        for (int id : selectedIds) {
            T object = fullMap.get(id);
            if (object != null) {
                result.add(object);
            }
        }

        return result;
    }

    /**
     * Возвращает список объектов из строки вида код:наименование; код:наименование.
     * @param value
     * @return
     */
    public static final List<IdTitle> parseIdTitleList(String value) {
        List<IdTitle> result = new ArrayList<IdTitle>();

        StringTokenizer st = new StringTokenizer(value, ";,");
        while (st.hasMoreTokens()) {
            String[] pair = st.nextToken().split(":");
            if (pair.length != 2) {
                continue;
            }

            result.add(new IdTitle(Utils.parseInt(pair[0]), pair[1]));
        }

        return result;
    }

    /**
     * Возвращает список объектов из строки вида код:наименование; код:наименование. Если значение для второго элемента пары не указано, будет использовано переданное в параметре noPairValue 
     * @param value
     * @param noPairValue
     * @return
     */
    public static final List<IdTitle> parseIdTitleList(String value, String noPairValue) {
        List<IdTitle> result = new ArrayList<IdTitle>();

        if (value == null) {
            return result;
        }

        if (value.contains(":")) {
            try {
                StringTokenizer st = new StringTokenizer(value, ";,");
                while (st.hasMoreTokens()) {
                    String nextToken = st.nextToken();

                    if (nextToken.indexOf(":") > 0) {
                        String[] pair = nextToken.split(":");

                        result.add(new IdTitle(Utils.parseInt(pair[0].replaceAll("[^\\d]", "")), pair[1].replaceAll("[^\\d]", "")));
                    } else {
                        result.add(new IdTitle(Utils.parseInt(nextToken.replaceAll("[^\\d]", "")), noPairValue));
                    }
                }
            } catch (Exception e) {
                return result;
            }
        } else {
            Set<Integer> groupIdSet = toIntegerSet(value);

            for (Integer groupId : groupIdSet) {
                result.add(new IdTitle(groupId, noPairValue));
            }
        }

        return result;
    }

    public static final void addSetupPair(StringBuilder data, String prefix, String param, String value) {
        data.append(prefix);
        data.append(param);
        data.append("=");
        data.append(value);
        data.append("\n");
    }

    /**
     * Добавляет в конец строки новое значение, 
     * отделяя запятой с пробелом, если он не первый.
     * 
     * @param result
     * @param value
     */
    public static final void addCommaSeparated(StringBuilder result, String value) {
        addSeparated(result, DEFAULT_DELIM, value);
    }

    public static final void addSeparated(StringBuilder result, String separator, String value) {
        if (result.length() != 0) {
            result.append(separator);
        }
        result.append(value);
    }

    /**
     * Генерирует строку из шаблона с подстановкой макросов вида ${param_<код параметра>}.
     * @param object
     * @param objectId
     * @param paramValueDAO
     * @param pattern
     * @return
     * @throws Exception
     */
    public static String formatPatternString(String object, int objectId, ParamValueDAO paramValueDAO, String pattern) throws Exception {
        String result = "";
        if (pattern != null) {
            result = pattern;
            Set<Integer> parameterIdList = new HashSet<Integer>();
            int last = 0;
            while (true) {
                int found = pattern.indexOf("${param_", last);
                if (found == -1) {
                    break;
                }
                last = found + 8;
                found = pattern.indexOf("}", last);
                parameterIdList.add(Utils.parseInt(pattern.substring(last, found)));
            }

            List<Parameter> paramList = ParameterCache.getObjectTypeParameterList(object, -1);
            List<ParameterValuePair> valueList = paramValueDAO.loadParameters(paramList, objectId, false);

            Map<Integer, ParameterValuePair> valueMap = new HashMap<Integer, ParameterValuePair>();
            for (ParameterValuePair value : valueList) {
                valueMap.put(value.getParameter().getId(), value);
            }
            for (Integer parameterId : parameterIdList) {
                ParameterValuePair parameter = valueMap.get(parameterId);
                String value = "";
                if (parameter != null) {
                    value = parameter.getValueTitle();
                    if (value == null) {
                        value = "";
                    }
                }
                result = result.replaceAll("\\$\\{param_" + parameterId + "\\}", value);
            }
        }
        return result;
    }

    public static final List<ListItem> parseList(Element listElement) {
        List<ListItem> list = new ArrayList<ListItem>();
        NodeList nodeList = listElement.getElementsByTagName("item");
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int index = 0; index < nodeList.getLength(); index++) {
                Element item = (Element) nodeList.item(index);
                ListItem listItem = new ListItem();
                listItem.setId(Integer.parseInt(item.getAttribute("id")));
                listItem.setTitle(item.getAttribute("title"));
                list.add(listItem);
            }
        }
        return list;
    }

    /**
     * Вызывает {@link #getDigest(String, String)} с кодировкой UTF-8.
     * @param value
     * @return
     */
    public static String getDigest(String value) {
        return getDigest(value, Utils.UTF8.name());
    }

    /**
     * Возвращает строковое представление MD5 хэша от входной строки.
     * @param value входные данные 
     * @param charset кодировка
     * @return строка
     */
    public static String getDigest(String value, String charset) {
        StringBuffer passwdDigest = new StringBuffer();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes(charset));
            byte[] digestBytes = digest.digest();
            for (int i = 0; i < digestBytes.length; i++) {
                passwdDigest.append(HEX[(digestBytes[i] & 0xF0) >> 4]);
                passwdDigest.append(HEX[digestBytes[i] & 0x0F]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return passwdDigest.length() == 0 ? null : passwdDigest.toString();
    }

    public static String getTmpDir() {
        return System.getProperty("java.io.tmpdir");
    }

    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$");

    public static final boolean validateEmail(String email) {
        return rfc2822.matcher(email).matches();
    }

    private static final Pattern ipv4_pattern = Pattern
            .compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    public static final boolean validateIPv4(String ip) {
        return ipv4_pattern.matcher(ip).matches();
    }

    public static final File createDirectoryIfNoExistInWorkDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            // каталог log взят, т.к. он обязательно есть в рабочем каталоге
            File logFile = new File("log");
            dir = new File(logFile.getAbsolutePath() + "/../" + dirName);
            dir.mkdir();
        }
        return dir;
    }

    /**
     * Возвращает остаток строки после num вхождения token в строку value.
     * @param value
     * @param token
     * @param num
     * @return
     */
    public static final String substringAfter(String value, String token, int num) {
        String result = "";

        int lastFindPos = 0;
        int findPos = 0;
        while ((findPos = value.indexOf(token, lastFindPos + 1)) >= 0 && num > 0) {
            num--;
            lastFindPos = findPos;
        }

        if (num == 0) {
            result = value.substring(lastFindPos + 1);
        }

        return result;
    }

    /*
     * Преобразует строку в тип Date с помошью SimpleDateFormat по шаблону dd.MM.yyyy
     * @param str Строка для преобразования
     * @return Дата, если не смогли отпарсить, то null
    //TODO: Сделать в DynActionForm getDateParam и формат в зависимости от локали.
    public static Date getDate( String str )
    {	
    	SimpleDateFormat format = new SimpleDateFormat( "dd.MM.yyyy" );
    	try
    	{
    		Date date = format.parse( str );
    		return date;
    	}
    	catch( Exception e )
    	{
    		return null;
    	}
    }
     */

    public static Date getDateFromMinutes(int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, minutes / 60);
        calendar.set(Calendar.MINUTE, minutes % 60);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTime();
    }

    /**
     * Преобразование массива байт в HEX строку.
     * @param bytes массив байт
     * @param upperCase если true, то символы результата в верхнем регистре
     * @return
     */
    public static String bytesToString(byte[] bytes, boolean upperCase) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        final char[] hex = upperCase ? Utils.HEX : Utils.HEX_LOWERCASE;
        final StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (int i = 0, size = bytes.length; i < size; i++) {
            sb.append(hex[(bytes[i] & 0xF0) >> 4]);
            sb.append(hex[bytes[i] & 0x0F]);
        }

        return sb.toString();
    }

    /**
     * Конвертирование HEX строки вида 0bcf224ba2 или 0BCF224BA2 в массив байт.
     * @param s строка вида 0bcf224ba2 или 0BCF224BA2
     * @return
     */
    public static byte[] stringToBytes(final String s) {
        if (Utils.isBlankString(s)) {
            return null;
        }

        final int size = s.length();

        int i, j;
        final byte[] result;
        // если количество цифр нечетное - считаем что первая цифра - 0
        if (size % 2 == 0) {
            i = j = 0;
            result = new byte[size / 2];
        } else {
            i = j = 1;
            result = new byte[size / 2 + 1];

            result[0] = (byte) Character.digit(s.charAt(0), 16);
        }

        while (i < size) {
            int digit1 = Character.digit(s.charAt(i++), 16);
            int digit2 = Character.digit(s.charAt(i++), 16);

            result[j++] = (byte) (digit1 * 16 + digit2);
        }

        return result;
    }

    /**
     * Возвращает первый элемент коллекции либо null если коллекция пуста.
     * 
     * @param collection
     * @return
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (collection.size() > 0) {
            return collection.iterator().next();
        }
        return null;
    }

    /**
     * Кодирует символы XML форматирования (кавычки, теги)
     * для нормального их отображения в HTML тексте. 
     * 
     * @param value
     * @return
     */
    public static String escapeXml(String value) {
        return StringEscapeUtils.escapeXml(value);
    }

    /**
     * Меняет местами четные символы с соответствующими нечетными
     * @param str входная строка
     * @return
     */
    public static String swapWords(String str) {
        StringBuilder result = new StringBuilder(str);
        final int size = result.length();
        for (int i = 0; i < size - 1; i += 2) {
            char oneChar = result.charAt(i);
            char twoChar = result.charAt(i + 1);
            result.setCharAt(i, twoChar);
            result.setCharAt(i + 1, oneChar);
        }
        return result.toString();
    }

    /**
     * Дамп полей объектов в строку.
     * @param objects
     * @return
     */
    public static String dump(Object... objects) {
        StringBuilder result = new StringBuilder();
        for (Object object : objects)
            if (object instanceof String)
                result.append(object.toString());
            else
                result.append(new ReflectionToStringBuilder(object).toString());
        return result.toString();
    }

    /**
     * Устанавливает заголовки HTTP запроса при выгрузке файла.
     * @param response
     * @param fileName
     */
    public static void setFileNameHeades(HttpServletResponse response, String fileName) {
        try {
            // application/octet-stream почему-то не предлагает открыть приложением по расширению
            response.setContentType("application/any");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
    * Преобразование байта в целое без знака.
    * @param value
    * @return
    */
    public static final int unsignedByteToInt(byte value) {
        int val = value;
        if (val < 0) {
            val &= 0x000000ff;
            val |= 0x00000080;
        }
        return val;
    }

    /**
     * Преобразование целого лонг без знака.
     * @param value
     * @return
     */
    public static final long unsignedIntToLong(int value) {
        long val = value;
        if (val < 0) {
            val &= 0x00000000ffffffffL;
            val |= 0x0000000080000000L;
        }

        return val;
    }

    /**
     * Возвращает десятичное число, полученное преобразованием шестнадцатеричного
     * @param bytes массив байтов: шестнадцатеричное число 
     * @return
     */
    public static int convertBytesToInt(byte[] bytes) {
        int result = 0;
        if (bytes != null && bytes.length == 4) {
            result = 0x000000ff & bytes[3] | 0x0000ff00 & (bytes[2] << 8) | 0x00ff0000 & (bytes[1] << 16) | 0xff000000 & (bytes[0] << 24);
        }

        return result;
    }

    /**
     * Возвращает шестнадцатеричное число (массив байтов), полученное преобразованием десятичного
     * @param value
     * @return
     */
    public static byte[] convertIntToBytes(int value) {
        byte[] byteValue = new byte[4];

        for (int i = 0; i < 4; i++) {
            byteValue[3 - i] = (byte) (value & 0x000000ff);
            value >>= 8;
        }

        return byteValue;
    }
}