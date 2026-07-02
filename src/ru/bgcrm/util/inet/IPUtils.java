package ru.bgcrm.util.inet;

import java.util.Base64;

import ru.bgcrm.util.Utils;

public class IPUtils {
    /**
     * Преобразует IP в виде десятичного числа со знаком (4 байта) к dotted
     * нотации
     *
     * @param ip
     * @return
     */
    public static final String convertIpToString(int ip) {
        String net = "0.0.0.0";
        if (ip != 0) {
            byte[] b = convertIntToBytes(ip);
            net = unsignedByteToInt(b[0]) + "." + unsignedByteToInt(b[1]) + "."
                    + unsignedByteToInt(b[2]) + "." + unsignedByteToInt(b[3]);
        }
        return net;
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
     * Преобразует BASE64 кодированный byte[] адрес в человекочитаемою строку.
     * @param base64Addr
     * @return
     */
    public static String base64ToString(String base64Addr) {
        if (Utils.isBlankString(base64Addr))
            return base64Addr;
        return IPUtils.convertIpToString(convertBytesToInt(Base64.getDecoder().decode(base64Addr)));
    }
}
