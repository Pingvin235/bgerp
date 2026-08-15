package org.bgerp.model.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.util.Utils;

public class PhoneFormat {
    private final Map<Integer, List<String>> prefixMap;

    public PhoneFormat(String formats) {
        prefixMap = prefixMap(formats);
    }

    public String format(String value) {
        return phoneToFormat(value);
    }

    private Map<Integer, List<String>> prefixMap(String formats) {
        if (Utils.isEmptyString(formats)) {
            return null;
        }

        // Initial sorting of prefixes by number of digits.
        // Result - a map, key is the number of digits in the prefix
        String[] prefixsArray = formats.split(",");
        Map<Integer, List<String>> prefixs = new HashMap<>();
        for (int i = 0; i < prefixsArray.length; ++i) {
            int countDigits = phoneUnformatted(prefixsArray[i]).length();
            List<String> list = prefixs.get(countDigits);
            if (list == null) {
                list = new ArrayList<>();
                prefixs.put(countDigits, list);
            }
            list.add(prefixsArray[i]);
        }

        return prefixs;
    }

    private String phoneToFormat(String value) {
        if (prefixMap == null || prefixMap.isEmpty()) {
            return value;
        }

        // Search for the prefix
        String prefix = "";
        for (int i = value.length(); i >= 0; --i) {
            List<String> prefixsList = prefixMap.get(i);
            if (prefixsList == null) {
                continue;
            }

            // Digits of our value
            String valueOnlyDigits = phoneUnformatted(value);
            for (String pref : prefixsList) {
                // Digits of the current prefix
                String prefixOnlyDigits = phoneUnformatted(pref);
                // Take only as many digits as there are in the prefix
                if (prefixOnlyDigits.length() <= valueOnlyDigits.length()) {
                    if (valueOnlyDigits.startsWith(prefixOnlyDigits)) {
                        prefix = pref;
                        break;
                    }
                }
            }

            if (!Utils.isBlankString(prefix)) {
                break;
            }
        }

        if (Utils.isBlankString(prefix)) {
            return value;
        }
        if (value.length() > prefix.length()) {
            value = value.substring(0, prefix.length());
        }

        // Arrange spaces according to the found prefix
        int posValue = 0, posResult = 0;
        StringBuilder resValue = new StringBuilder();
        while (true) {
            if (posResult >= prefix.length() || posValue >= value.length()) {
                break;
            }
            if (Character.isDigit(prefix.charAt(posResult)) || prefix.charAt(posResult) == 'X') {
                resValue.append(value.charAt(posValue));
                posValue++;
                posResult++;
            } else {
                resValue.append(prefix.charAt(posResult));
                posResult++;
            }
        }
        if (posValue < value.length()) {
            resValue.append(value.substring(posValue));
        }

        return resValue.toString();
    }

    /**
     * Converts a formatted phone number into a set of digits
     * @param phone +7 (347) 2 924-823
     * @return 73472924823
     */
    // can theoretically be called often, and the logic is simple enough to avoid creating Matcher objects each time
    private String phoneUnformatted(String phone) {
        if (Utils.isEmptyString(phone)) {
            return "";
        }

        StringBuilder sb = null;

        for (int i = 0, size = phone.length(); i < size; i++) {
            char c = phone.charAt(i);

            if (Character.isDigit(c)) {
                if (sb != null) {
                    sb.append(c);
                }
            } else {
                if (sb == null) {
                    sb = new StringBuilder(phone.length());

                    for (int j = 0; j < i; j++) {
                        sb.append(phone.charAt(j));
                    }
                }
            }
        }

        if (sb != null) {
            return sb.toString();
        } else {
            return phone;
        }
    }

}
