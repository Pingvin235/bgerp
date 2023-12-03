package org.bgerp.util;

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

        // Первоначальная сортировка префиксов по количеству цифр.
        // Результат - мэп, ключ - количество цифр в префиксе
        String[] prefixsArray = formats.split(",");
        Map<Integer, List<String>> prefixs = new HashMap<Integer, List<String>>();
        for (int i = 0; i < prefixsArray.length; ++i) {
            int countDigits = phoneUnformatted(prefixsArray[i]).length();
            List<String> list = prefixs.get(countDigits);
            if (list == null) {
                list = new ArrayList<String>();
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

        //  Поиск префикса
        String prefix = "";
        for (int i = value.length(); i >= 0; --i) {
            List<String> prefixsList = prefixMap.get(i);
            if (prefixsList == null) {
                continue;
            }

            // Цифры нашего значения
            String valueOnlyDigits = phoneUnformatted(value);
            for (String pref : prefixsList) {
                // Цифры текущего префикса
                String prefixOnlyDigits = phoneUnformatted(pref);
                // Берем только такое количество цифр, сколько есть в префиксе
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

        // Расставляем пробелы в соответствии с найденным префиксом
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
     * Превращает форматированный телефон в набор цифр
     * @param phone +7 (347) 2 924-823
     * @return 73472924823
     */
    // вызываться теоретически может часто, а логика довольно простая, чтобы создавать каждый раз объекты Matcher.
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
