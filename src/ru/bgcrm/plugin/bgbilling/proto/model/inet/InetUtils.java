package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import java.util.regex.Pattern;

import ru.bgcrm.util.Utils;

public final class InetUtils {
    private static final Pattern macClearPattern = Pattern.compile("[\\s\\.:\\-]+");

    public static byte[] parseMacAddress(String macAddress) {
        if (Utils.isBlankString(macAddress)) {
            return null;
        }

        macAddress = macClearPattern.matcher(macAddress).replaceAll("");

        return toBytes(macAddress);
    }

    public static String macAddressToString(byte[] macAddress) {
        if (macAddress == null || macAddress.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(macAddress.length * 3);

        for (int i = 0, size = macAddress.length; i < size; i++) {
            sb.append(Utils.HEX[(macAddress[i] & 0xF0) >> 4]);
            sb.append(Utils.HEX[macAddress[i] & 0x0F]);

            sb.append(':');
        }

        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    private static byte[] toBytes(String s) {
        if (Utils.isBlankString(s)) {
            return null;
        }

        int size = s.length();

        int i, j;
        final byte[] result;
        // если количество цифр нечетное - считаем что первая цифра - 0
        if (size % 2 == 0) {
            i = j = 0;
            result = new byte[size / 2];
        } else {
            i = j = 1;
            result = new byte[size / 2 + 1];

            int digit2 = Character.digit(s.charAt(0), 16);
            result[0] = (byte) digit2;
        }

        while (i < size) {
            int digit1 = Character.digit(s.charAt(i++), 16);
            int digit2 = Character.digit(s.charAt(i++), 16);

            result[j++] = (byte) (digit1 * 16 + digit2);
        }

        return result;
    }
}
