package ru.bgcrm.model.param;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ru.bgcrm.util.PatternFormatter;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

public class ParameterPhoneValueItem {
    private String phone = "";
    private String format;
    private String comment = "";
    private int flags = 0;

    public ParameterPhoneValueItem() {
    }

    public ParameterPhoneValueItem(String phone, String format, String comment) {
        this.phone = phone;
        this.format = format;
        this.comment = comment;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public String[] getPhoneParts() {
        String[] result = new String[] { "", "", phone };

        String defaultPrefix = Setup.getSetup().get("param.phone.default.prefix", "3472");
        if (phone.matches("^[0-9]{11}$")) {
            if (format == null || !format.matches("^[0-9]{2}$")) {
                if (phone.startsWith("7" + defaultPrefix) || phone.startsWith("8" + defaultPrefix)) {
                    format = "13";
                } else {
                    format = "10";
                }
            }
            int p1 = Utils.parseInt(format.substring(0, 1));
            int p2 = Utils.parseInt(format.substring(1));
            result[0] = phone.substring(0, p1);
            result[1] = phone.substring(p1, p1 + p2);
            result[2] = phone.substring(p1 + p2);
        }

        return result;
    }

    private final static String defaultPattern = "(${number})( [${comment}]);";

    /**
     * Получить результирующую строку параметра типа телефон
     * 
     * @param setup Setup
     * @param pd  код параметра
     * @param phones список телефонов
     * @param formats список форматов телефонов
     * @param comments список комментариев
     * @return
     */
    public static final String getPhones(List<ParameterPhoneValueItem> items) {
        String pattern = Setup.getSetup().get("param.phone.format", defaultPattern);

        StringBuffer result = new StringBuffer();

        for (ParameterPhoneValueItem item : items) {
            String val = PatternFormatter.insertPatternPart(pattern, "number",
                    formatPhone(item.getFormat(), item.getPhone()));
            val = PatternFormatter.insertPatternPart(val, "comment", item.getComment());

            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(val);
        }

        return result.toString();
    }

    /**
     * Форматирует номер по шаблону вида +X XXX-XXX-XX-XX.
     * Шаблон выбирается из конфигурации в зависимости от переменной format.
     */
    public static final String formatPhone(String format, String phone) {
        Setup setup = Setup.getSetup();

        final String prefix = "param.phone.format.number";

        String pattern = setup.get(prefix + ".f" + format);
        if (pattern == null) {
            pattern = setup.get(prefix);
        }
        if (pattern == null) {
            if ("10".equals(format)) {
                pattern = "+X XXX-XXX-XX-XX";
            } else if ("13".equals(format)) {
                pattern = "+X (XXX) XXX-XX-XX";
            } else if ("14".equals(format)) {
                pattern = "+X (XXXX) XX-XX-XX";
            } else if ("15".equals(format)) {
                pattern = "+X (XXXXX) X-XX-XX";
            } else {
                pattern = "+X XXX XXX-XX-XX";
            }
        }

        final int patternLength = pattern.length();
        final int numberLength = phone.length();

        int numberPointer = 0;

        StringBuilder result = new StringBuilder(patternLength);
        for (int i = 0; i < patternLength && numberPointer < numberLength; i++) {
            char c = pattern.charAt(i);
            if (c == 'X') {
                result.append(phone.charAt(numberPointer++));
            } else {
                result.append(pattern.charAt(i));
            }
        }

        return result.toString();
    }
    
    @Override
    public int hashCode() {
        return phone.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ParameterPhoneValueItem && ((ParameterPhoneValueItem) obj).phone.equals(phone);
    }
}