package ru.bgcrm.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;

import org.apache.taglibs.standard.functions.Functions;
import org.bgerp.app.servlet.jsp.UtilFunction;
import org.bgerp.cache.ParameterCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.base.Id;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.iface.Title;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.param.ParameterValuePair;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

@Dynamic
public class Utils {
    private static final Log log = Log.getLog();

    /** Default delimiter: ", " */
    public static final String DEFAULT_DELIM = ", ";

    public static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    public static final char[] HEX_LOWERCASE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static final String TMP_DIR = Utils.getSystemProperty("tmpdir", "tmp");
    static {
        File tmpDir = new File(TMP_DIR);
        if (!tmpDir.exists()) {
            log.info("Creating tmp dir: {}", TMP_DIR);
            tmpDir.mkdir();
        }
    }

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
     * Converts an object's string representation to a decimal object.
     * @param obj the object.
     * @param defaultValue the default value in case of parsing exception.
     * @return decimal object, {@code defaultValue} for any unparsable value.
     */
    public static BigDecimal parseBigDecimal(final Object obj, final BigDecimal defaultValue) {
        try {
            return new BigDecimal(String.valueOf(obj));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Converts an object's string representation to a decimal object.
     * @param obj the object.
     * @return decimal object, {@code null} when {@code str} was {@code null}, {@link BigDecimal#ZERO} for any unparsable value.
     */
    public static BigDecimal parseBigDecimal(final Object obj) {
        if (obj == null)
            return null;
        return parseBigDecimal(obj, BigDecimal.ZERO);
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
     * @return true - если str равно "1", "TRUE", "YES", "ON" без учёта регистра; false - если str равно "0", "FALSE", "NO" без учёта регистра; в ином случае - defaultValue.
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
     * Checks if string value is blank.
     * @param value checked value.
     * @return {@code false} when {@code value} is {@code null}, empty or has only whitespace chars.
     */
    public static final boolean notBlankString(String value) {
        return value != null && value.trim().length() > 0;
    }

    /**
     * Checks all the passed values with {@link #notBlankString(String)}.
     * @param value values.
     * @return {@code value} is not null, all the values aren't blank.
     */
    public static final boolean notBlankStrings(String... value) {
        if (value == null)
            return false;
        for (String val : value) {
            if (isBlankString(val))
                return false;
        }
        return true;
    }

    /**
     * Is the integer value greater than zero. To do not create lambda functions.
     * @param value
     * @return
     */
    public static final boolean isPositive(Integer value) {
        return value > 0;
    }

    /**
     * Is the string a valid E-Mail address.
     * @param value
     * @return
     */
    public static final boolean isValidEmail(String value) {
        if (isBlankString(value))
            return false;
        try {
            new InternetAddress(value).validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
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
        List<Integer> result = new ArrayList<>();

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
        Set<Integer> result = new LinkedHashSet<>();

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
        Set<String> result = new LinkedHashSet<>();
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
        Set<String> result = new HashSet<>();
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
     * Converts a string with obitary delimeter chars to list of strings.
     * @param value incoming string.
     * @param delims delimeter chars.
     * @return list of tokens, each of that is not empty string.
     */
    public static final List<String> toList(String value, String delims) {
        List<String> result = new ArrayList<>();

        if (notBlankString(value)) {
            StringTokenizer st = new StringTokenizer(value.trim(), delims);
            while (st.hasMoreTokens()) {
                result.add(st.nextToken().trim());
            }
        }

        return result;
    }

    /**
     * Converts a string with obitary delimeter chars to set of strings.
     * @param value incoming string.
     * @param delims delimeter chars.
     * @return set of tokens, each of that is not empty string.
     */
    public static final String toText(List<String> config, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object configLine : config.toArray()) {
            sb.append(configLine).append(separator);
        }
        return sb.toString();
    }

    /**
     * Возвращает список кодов объектов.
     * @param list
     * @return
     */
    public static final <T extends IdTitle> List<Integer> getObjectIdsList(Collection<T> list) {
        List<Integer> result = new ArrayList<>(list.size());
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
        Set<Integer> result = new HashSet<>(list.size());
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
     * Concatenates object titles to a comma separated string.
     * @param list the list of titled objects
     * @return comma separated string
     */
    public static final <T extends Title> String getObjectTitles(Collection<T> list) {
        return list == null ? "" : list.stream().map(Title::getTitle).collect(Collectors.joining(DEFAULT_DELIM));
    }

    /**
     * Concatenates object titles to a comma separated string.
     * @param fullList the full object list, defines the resulting order
     * @param selectedIds the selected IDs
     * @return comma separated string
     */
    public static <T extends IdTitle> String getObjectTitles(List<T> fullList, Set<Integer> selectedIds) {
        return Utils.getObjectTitles(Utils.getObjectList(fullList, selectedIds));
    }

    /**
     * Concatenates object titles to a comma separated string.
     * @param fullMap the full object map
     * @param selectedIds the selected IDs, defines the resulting order
     * @return comma separated string
     */
    public static final <T extends IdTitle> String getObjectTitles(Map<Integer, T> fullMap, List<Integer> selectedIds) {
        return Utils.getObjectTitles(Utils.getObjectList(fullMap, selectedIds));
    }

    /**
     * Selects objects sub-list from a given full list with IDs presented in a collection.
     * @param fullList the full list.
     * @param selectedIds the IDs collection.
     * @return
     */
    public static final <T extends IdTitle> List<T> getObjectList(List<T> fullList, Collection<Integer> selectedIds) {
        List<T> result = new ArrayList<>();

        for (T object : fullList) {
            if (selectedIds != null && selectedIds.contains(object.getId())) {
                result.add(object);
            }
        }

        return result;
    }

    /**
     * Selects objects list from a full map by a given IDs list in the same order.
     * @param fullMap the full objects map.
     * @param selectedIds the IDs list.
     * @return
     */
    public static final <T extends IdTitle> List<T> getObjectList(Map<Integer, T> fullMap, List<Integer> selectedIds) {
        List<T> result = new ArrayList<>();

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
        List<IdTitle> result = new ArrayList<>();

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
        List<IdTitle> result = new ArrayList<>();

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
            Set<Integer> parameterIdList = new HashSet<>();
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

            Map<Integer, ParameterValuePair> valueMap = new HashMap<>();
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

    /**
     * Вызывает {@link #getDigest(String, String)} с кодировкой UTF-8.
     * @param value
     * @return
     */
    public static String getDigest(String value) {
        return getDigest(value, StandardCharsets.UTF_8.name());
    }

    /**
     * Возвращает строковое представление MD5 хэша от входной строки.
     * @param value входные данные
     * @param charset кодировка
     * @return строка
     */
    public static String getDigest(String value, String charset) {
        try {
            return getDigest(value.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            return null;
        }
    }

    /**
     * HEX representation of MD5 digest.
     * @param value digest basic.
     * @return digest HEX string or {@code null} in case of any error.
     */
    public static String getDigest(byte[] value) {
        StringBuffer passwdDigest = new StringBuffer(32);
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(value);
            byte[] digestBytes = digest.digest();
            for (int i = 0; i < digestBytes.length; i++) {
                passwdDigest.append(HEX[(digestBytes[i] & 0xF0) >> 4]);
                passwdDigest.append(HEX[digestBytes[i] & 0x0F]);
            }
        } catch (Exception ex) {
            log.error(ex);
        }
        return passwdDigest.length() == 0 ? null : passwdDigest.toString();
    }

    /**
     * System temp directory.
     * @return value of system property 'java.io.tmpdir', or '/tmp' if it is missing.
     */
    public static String getTmpDir() {
        return TMP_DIR;
    }

    /**
     * @see #isValidEmail(String).
     */
    @Deprecated
    public static final boolean validateEmail(String email) {
        return isValidEmail(email);
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

    /**
     * @return generated random string with 32 ASCII chars.
     */
    public static final String generateSecret() {
        byte[] random = new byte[32];
        new Random().nextBytes(random);
        return Utils.getDigest(random);
    }

    /**
     * @param collection collection of elements.
     * @return the first element from {@code collection}, or {@code null} if collection is {@code null} or empty.
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (collection != null && collection.size() > 0) {
            return collection.iterator().next();
        }
        return null;
    }

    /**
     * Calls {@link Functions#escapeXml(String)} - replaces XML markup symbols to special codes.
     * @param value
     * @return
     */
    public static String escapeXml(String value) {
        return Functions.escapeXml(value);
    }

    /**
     * Calls {@link UtilFunction#htmlEncode(String)} - replaces only HTML tags.
     * @param value
     * @return
     */
    public static String htmlEncode(String value) {
        return UtilFunction.htmlEncode(value);
    }

    /**
     * Extracts entity ID from URL.
     * @param url URL.
     * @return extracted positive ID or {@code 0} if couldn't extract.
     */
    public static int getOpenId(String url) {
        if (isBlankString(url))
            return 0;

        int posFrom = url.lastIndexOf('/');
        if (posFrom == -1)
            return 0;

        int posTo = url.indexOf('?', posFrom);
        if (posTo == -1)
            posTo = url.indexOf('#', posFrom);
        if (posTo == -1)
            posTo = url.length();

        return parseInt(url.substring(posFrom + 1, posTo));
    }

    /**
     * Sets HTTP headers for downloaded file.
     * @param response
     * @param fileName
     */
    public static void setFileNameHeaders(HttpServletResponse response, String fileName) {
        try {
            response.setContentType(URLConnection.guessContentTypeFromName(fileName));
            response.setHeader("Content-Disposition", "attachment;filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }
    }

    /**
     * Writes error message and exists the running application.
     * @param code exit code.
     * @param message message.
     */
    public static void errorAndExit(int code, String message) {
        System.err.println(message);
        System.err.flush();
        System.exit(code);
    }

    /**
     * The method does special JSP-specific type converting, therefore must not be called from Java code.
     * @see {@link Bean#newInstance(String)).
     */
    @Deprecated
    public static Object newInstance(String className, Object... args) throws Exception {
        return UtilFunction.newInstance(className, args);
    }

    /**
     * Checks if object is instance one of classes.
     * @param o the object to check.
     * @param names class names.
     * @return if object {@code o} is instance any of {@code names} classes.
     */
    public static boolean hasClass(Object o, String... names) {
        for (String name : names) {
            if (o.getClass().getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a property value from {@link System#getProperty(String)}.
     * @param key the key is prepended by {@code bgerp.}
     * @param defaultValue the default value if no property found.
     * @return
     */
    public static String getSystemProperty(String key, String defaultValue) {
        if (key.startsWith("bgerp"))
            throw new IllegalArgumentException("Do not use 'bgerp' prefix for a key");
        return System.getProperty("bgerp." + key, defaultValue);
    }

    /**
     * Formats a decimal value to a regular string without trailing zeros.
     * @param value the decimal value.
     * @return empty string for {@code null} value, or formatted string.
     */
    public static String format(BigDecimal value) {
        if (value == null)
            return "";

        return value.stripTrailingZeros().toPlainString();
    }
}