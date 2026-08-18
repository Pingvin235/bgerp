package ru.bgcrm.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.taglibs.standard.functions.Functions;
import org.bgerp.app.servlet.jsp.UtilFunction;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.iface.Title;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

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
     * Converts a string to an int, return {@code 0} in case of error
     * @param str the input string
     * @return the number converted from the string, or {@code 0} in case of error
     */
    public static int parseInt(String str) {
        return parseInt(str, 0);
    }

    /**
     * Converts a string to an int, return a default value in case of error
     * @param str the input string
     * @param defaultValue the default value in case of error
     * @return the number converted from the string, or {@code defaultValue} in case of error
     */
    public static int parseInt(String str, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt(str.trim());
        } catch (Exception ex) {
        }
        return value;
    }

    /**
     * Converts a string to a long, return {@code 0} in case of error
     * @param str the input string
     * @return the number converted from the string, or {@code 0} in case of error
     */
    public static long parseLong(String str) {
        return parseLong(str, 0L);
    }

    /**
     * Converts a string to a long, return a default value in case of error
     * @param str the input string
     * @param defaultValue the default value in case of error
     * @return the number converted from the string, or {@code defaultValue} in case of error
     */
    public static long parseLong(String str, long defaultValue) {
        long value = defaultValue;
        try {
            value = Long.parseLong(str.trim());
        } catch (Exception ex) {
        }
        return value;
    }

    /**
     * Converts an object's string representation to a decimal object
     * @param obj the object
     * @param defaultValue the default value in case of parsing exception
     * @return decimal object, {@code defaultValue} for any unparsable value
     */
    public static BigDecimal parseBigDecimal(final Object obj, final BigDecimal defaultValue) {
        try {
            return new BigDecimal(String.valueOf(obj));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Converts an object's string representation to a decimal object
     * @param obj the object
     * @return decimal object, {@code null} when {@code obj} was {@code null}, {@link BigDecimal#ZERO} for any unparsable value
     */
    public static BigDecimal parseBigDecimal(final Object obj) {
        if (obj == null)
            return null;
        return parseBigDecimal(obj, BigDecimal.ZERO);
    }

    /**
     * Converts a string to a boolean, return {@code false} in case of error
     * @param str the input string
     * @return the boolean value converted from the string, or {@code false} in case of error
     */
    public static boolean parseBoolean(String str) {
        return parseBoolean(str, false);
    }

    /**
     * Checks whether the string contains only digits, i.e. no letters
     * @param str the input string
     * @return {@code true} if the string doesn't contain letters
     */
    public static boolean isStringNumber(String str) {
        for (int i = 0; i < str.length(); i++)
            if (Character.isLetter(str.charAt(i))) {
                return false;
            }
        return true;
    }

    /**
     * Converts a string to a boolean
     * @param str the string
     * @param defaultValue the default value
     * @return {@code true} - for {@code str} "1", "TRUE", "YES", "ON" case insensitive; {@code false} - for {@code str} "0", "FALSE", "NO" case insensitive; all other cases - {@code defaultValue}
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
     * Converts a boolean value to a string "1" or "0"
     * @param value the boolean value
     * @return "1" for {@code true}, "0" for {@code false}
     */
    public static final String booleanToStringInt(boolean value) {
        return value ? "1" : "0";
    }

    /**
     * Returns the value, or a default value if the value is {@code null}
     * @param value the value
     * @param defaultValue the default value
     * @return {@code value}, or {@code defaultValue} if {@code value} is {@code null}
     */
    public static final <T> T maskNull(final T value, final T defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Converts the input string to an empty one if it is {@code null}
     * @param value the input string
     * @return {@code value}, or an empty string if {@code value} is {@code null}
     */
    public static final String maskNull(final String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns the input value if it is not {@code null}, otherwise {@link BigDecimal#ZERO}
     * @param value the input value
     * @return {@code value}, or {@link BigDecimal#ZERO} if {@code value} is {@code null}
     */
    public static final BigDecimal maskNullDecimal(final BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }


    /**
     * Converts to {@code null} an empty collection
     * @param value the input collection
     * @return the input collection, if it is not empty, or {@code null}
     */
    public static final <T, C extends Collection<T>> C emptyToNull(C value) {
        if (value == null || value.size() == 0) {
            return null;
        }
        return value;
    }

    /**
     * Converts the input string to a default value if it is {@code null} or empty
     * @param value the input string
     * @param defaultValue the default value
     * @return {@code value}, or {@code defaultValue} if {@code value} is {@code null} or empty
     */
    public static final String maskEmpty(final String value, final String defaultValue) {
        return isEmptyString(value) ? defaultValue : value;
    }

    /**
     * Checks whether the string is empty or {@code null}
     * @param value the checked string
     * @return {@code true} if empty or {@code null}
     */
    public static final boolean isEmptyString(String value) {
        return value == null || value.length() == 0;
    }

    /**
     * Checks whether the string is empty, consists only of whitespace chars, or is {@code null}
     * @param value the checked string
     * @return {@code true} if empty, blank, or {@code null}
     */
    public static final boolean isBlankString(String value) {
        return value == null || value.trim().length() == 0;
    }

    /**
     * Checks whether the string is not empty and not {@code null}
     * @param value the checked string
     * @return {@code false} if empty or {@code null}
     */
    public static final boolean notEmptyString(String value) {
        return value != null && value.length() > 0;
    }

    /**
     * Checks if string value is blank
     * @param value checked value
     * @return {@code false} when {@code value} is {@code null}, empty or has only whitespace chars
     */
    public static final boolean notBlankString(String value) {
        return value != null && value.trim().length() > 0;
    }

    /**
     * Checks all the passed values with {@link #notBlankString(String)}
     * @param value values
     * @return {@code value} is not null, all the values aren't blank
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
     * Counts the number of lines in string
     * @param value the string
     * @return the count of {@code \n} chars
     */
    public static final int countLines(String value) {
        if (value == null)
            return 0;

        int count = 0;
        int index = value.indexOf('\n');

        while (index != -1) {
            count++;
            index = value.indexOf('\n', index + 1);
        }

        return count;
    }

    /**
     * Is the integer value greater than zero. To do not create lambda functions.
     * @param value the value
     * @return {@code true} if {@code value} is greater than zero
     */
    public static final boolean isPositive(Integer value) {
        return value > 0;
    }

    /**
     * Is the string a valid email address
     * @param value the checked string
     * @return {@code true} if the string is a valid email address
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
     * Converts a collection to a string joined with {@link #DEFAULT_DELIM}
     * @param valuesList the collection
     * @return the resulting string, or an empty string if the collection is empty
     */
    public static final String toString(Collection<?> valuesList) {
        return toString(valuesList, "", DEFAULT_DELIM);
    }

    /**
     * Converts a collection to a string joined with the delimiter
     * @param valuesList the collection
     * @param emptyValue the value to return for an empty list
     * @param delim the delimiter
     * @return the resulting string
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
     * Converts a string, delimited by commas or semicolons, to a list of {@link Integer}
     * @param valuesStr the input string
     * @return the resulting list
     */
    public static final List<Integer> toIntegerList(String valuesStr) {
        return toIntegerList(valuesStr, ",;");
    }

    /**
     * Converts a string, delimited by the given chars, to a list of {@link Integer}
     * @param valuesStr the input string
     * @param delims the delimiter chars
     * @return the resulting list
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
     * Converts a string, delimited by commas, to a set of {@link Integer}
     * @param valuesStr the input string
     * @return the resulting set
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
     * Converts a string, delimited by the given chars, to a set of strings
     * @param valuesStr the input string
     * @param delims the delimiter chars
     * @return the resulting set
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
     * Converts a string, delimited by commas or semicolons, to a set of strings
     * @param valuesStr the input string
     * @return the resulting set
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
     * Converts a string, delimited by commas or semicolons, to a list of strings
     * @param valuesStr the input string
     * @return the resulting list
     */
    public static final List<String> toList(String valuesStr) {
        return toList(valuesStr, ",;");
    }

    /**
     * Converts a string with arbitrary delimiter chars to list of strings
     * @param value incoming string
     * @param delims delimiter chars
     * @return list of tokens, each of that is not empty string
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
     * Joins configuration lines to a single string with a separator
     * @param config configuration lines
     * @param separator separator placed after each line
     * @return joined string
     */
    public static final String toText(List<String> config, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object configLine : config.toArray()) {
            sb.append(configLine).append(separator);
        }
        return sb.toString();
    }

    /**
     * Returns a list of object IDs
     * @param list the objects
     * @return the resulting list
     */
    public static final <T extends IdTitle> List<Integer> getObjectIdsList(Collection<T> list) {
        List<Integer> result = new ArrayList<>(list.size());
        for (T object : list) {
            result.add(object.getId());
        }
        return result;
    }

    /**
     * Returns a set of object IDs
     * @param list the objects
     * @return the resulting set
     */
    public static final <T extends IdTitle> Set<Integer> getObjectIdsSet(Collection<T> list) {
        Set<Integer> result = new HashSet<>(list.size());
        for (T object : list) {
            result.add(object.getId());
        }
        return result;
    }

    /**
     * Comma separated object IDs
     * @param values the objects
     * @return the resulting string
     */
    public static final <T extends org.bgerp.model.base.iface.Id<Integer>> String getObjectIds(Collection<T> values) {
        return getObjectIds(values, null);
    }

    /**
     * Comma separated object IDs
     * @param values the objects
     * @param startValues beginning of the resulting string
     * @return the resulting string
     */
    public static final <T extends org.bgerp.model.base.iface.Id<Integer>> String getObjectIds(Collection<T> values, String startValues) {
        return getObjectIds(values, startValues, DEFAULT_DELIM);
    }

    /**
     * Separated object IDs
     * @param values the objects
     * @param startValues beginning of the resulting string
     * @param delim the separator
     * @return the resulting string
     */
    public static <T extends org.bgerp.model.base.iface.Id<Integer>> String getObjectIds(Collection<T> values, String startValues, String delim) {
        StringBuilder result = new StringBuilder();
        if (notEmptyString(startValues)) {
            result.append(startValues);
        }

        if (values != null) {
            for (T object : values) {
                if (result.length() != 0) {
                    result.append(delim);
                }
                result.append(object.getId());
            }
        }

        return result.toString();
    }

    /**
     * Concatenates object titles to a comma separated string
     * @param list the list of titled objects
     * @return comma separated string
     */
    public static final <T extends Title> String getObjectTitles(Collection<T> list) {
        return list == null ? "" : list.stream().map(Title::getTitle).collect(Collectors.joining(DEFAULT_DELIM));
    }

    /**
     * Concatenates object titles to a comma separated string
     * @param fullList the full object list, defines the resulting order
     * @param selectedIds the selected IDs
     * @return comma separated string
     */
    public static <T extends IdTitle> String getObjectTitles(List<T> fullList, Set<Integer> selectedIds) {
        return Utils.getObjectTitles(Utils.getObjectList(fullList, selectedIds));
    }

    /**
     * Concatenates object titles to a comma separated string
     * @param fullMap the full object map
     * @param selectedIds the selected IDs, defines the resulting order
     * @return comma separated string
     */
    public static final <T extends IdTitle> String getObjectTitles(Map<Integer, T> fullMap, List<Integer> selectedIds) {
        return Utils.getObjectTitles(Utils.getObjectList(fullMap, selectedIds));
    }

    /**
     * Selects objects sub-list from a given full list with IDs presented in a collection
     * @param fullList the full list
     * @param selectedIds the IDs collection
     * @return the resulting sub-list
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
     * Selects objects list from a full map by a given IDs list in the same order
     * @param fullMap the full objects map
     * @param selectedIds the IDs list
     * @return the resulting list
     */
    public static final <T extends IdTitle> List<T> getObjectList(Map<Integer, T> fullMap, List<Integer> selectedIds) {
        List<T> result = new ArrayList<>();

        if (fullMap != null && selectedIds != null) {
            for (int id : selectedIds) {
                T object = fullMap.get(id);
                if (object != null) {
                    result.add(object);
                }
            }
        }

        return result;
    }

    /**
     * Parses a list of objects from a string like "id:title; id:title"
     * @param value the input string
     * @return the resulting list
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
     * Parses a list of objects from a string like "id:title; id:title". If the title part of a pair is missing, {@code noPairValue} is used instead.
     * @param value the input string
     * @param noPairValue the default title value for entries without one
     * @return the resulting list
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

    /**
     * Appends a "prefix param=value" line to the data
     * @param data the target buffer
     * @param prefix the line prefix
     * @param param the parameter name
     * @param value the parameter value
     */
    public static final void addSetupPair(StringBuilder data, String prefix, String param, String value) {
        data.append(prefix);
        data.append(param);
        data.append("=");
        data.append(value);
        data.append("\n");
    }

    /**
     * Appends a new value to the end of the string, separating it with a comma and a space if it is not the first one
     * @param result the target buffer
     * @param value the value to append
     */
    public static final void addCommaSeparated(StringBuilder result, String value) {
        addSeparated(result, DEFAULT_DELIM, value);
    }

    /**
     * Appends a new value to the end of the string, separating it with the given separator if it is not the first one
     * @param result the target buffer
     * @param separator the separator
     * @param value the value to append
     */
    public static final void addSeparated(StringBuilder result, String separator, String value) {
        if (result.length() != 0) {
            result.append(separator);
        }
        result.append(value);
    }

    /**
     * Computes the MD5 digest HEX string of the input string, using UTF-8 encoding
     * @param value the input string
     * @return digest HEX string or {@code null} in case of any error
     */
    public static String getDigest(String value) {
        return getDigest(value, StandardCharsets.UTF_8.name());
    }

    /**
     * Returns the HEX string representation of the MD5 hash of the input string
     * @param value the input data
     * @param charset the encoding
     * @return digest HEX string or {@code null} in case of any error
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
     * HEX representation of MD5 digest
     * @param value digest basic
     * @return digest HEX string or {@code null} in case of any error
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
     * System temp directory
     * @return value of system property 'java.io.tmpdir', or '/tmp' if it is missing
     */
    public static String getTmpDir() {
        return TMP_DIR;
    }

    /**
     * Creates a directory with a given name in working directory if it does not exist
     * @param dirName the directory name
     * @return created or existing directory
     */
    public static final File createDirectoryIfNoExistInWorkDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            // the 'log' directory must be always there
            File logFile = new File("log");
            dir = new File(logFile.getAbsoluteFile().getParentFile(), dirName);
            log.debug("Creating directory: {}", dir.getAbsolutePath());
            dir.mkdir();
        }
        return dir;
    }

    /**
     * Returns the remainder of the string after the {@code num}-th occurrence of the token
     * @param value the input string
     * @param token the token to search for
     * @param num the occurrence number
     * @return the remaining substring, or an empty string if not found
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
     * @return generated random string with 32 ASCII chars
     */
    public static final String generateSecret() {
        byte[] random = new byte[32];
        new Random().nextBytes(random);
        return Utils.getDigest(random);
    }

    /**
     * @param collection collection of elements
     * @return the first element from {@code collection}, or {@code null} if collection is {@code null} or empty
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (collection != null && collection.size() > 0) {
            return collection.iterator().next();
        }
        return null;
    }

    /**
     * Replaces XML markup symbols with special codes
     * @param value the input string
     * @return the input string with replacements
     */
    public static String escapeXml(String value) {
        if (isBlankString(value))
            return value;
        return Functions.escapeXml(value);
    }

    /**
     * Replaces only HTML tags with escaped codes
     * @param value the input string
     * @return the input string with replacements
     */
    public static String htmlEncode(String value) {
        return UtilFunction.htmlEncode(value);
    }

    /**
     * Extracts entity ID from URL
     * @param url URL
     * @return extracted positive ID or {@code 0} if couldn't extract
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
     * Sets HTTP headers for downloaded file
     * @param response the HTTP response
     * @param fileName the file name
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
     * Writes error message and exits the running application
     * @param code exit code
     * @param message message
     */
    public static void errorAndExit(int code, String message) {
        System.err.println(message);
        System.err.flush();
        System.exit(code);
    }

    /**
     * The method does special JSP-specific type converting, therefore must not be called from Java code
     * @see org.bgerp.app.cfg.bean.Bean#newInstance(String)
     */
    @Deprecated
    public static Object newInstance(String className, Object... args) throws Exception {
        log.warndMethod("u.newInstance", "u:newInstance0");
        return UtilFunction.newInstance(className, args);
    }

    /**
     * Checks if object is instance one of classes
     * @param o the object to check
     * @param names class names
     * @return if object {@code o} is instance any of {@code names} classes
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
     * Retrieves a property value from {@link System#getProperty(String)}
     * @param key the key is prepended by {@code bgerp.}
     * @param defaultValue the default value if no property found
     * @return the property value, or {@code defaultValue} if not found
     */
    public static String getSystemProperty(String key, String defaultValue) {
        if (key.startsWith("bgerp"))
            throw new IllegalArgumentException("Do not use 'bgerp' prefix for a key");
        return System.getProperty("bgerp." + key, defaultValue);
    }

    /**
     * Formats a decimal value to a regular string without trailing zeros
     * @param value the decimal value
     * @return empty string for {@code null} value, or formatted string
     */
    public static String format(BigDecimal value) {
        if (value == null)
            return "";

        return value.stripTrailingZeros().toPlainString();
    }
}
