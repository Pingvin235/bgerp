package ru.bgcrm.util.inet;

import java.util.Base64;

import ru.bgcrm.util.Utils;

public class IPUtils {
    /**
     * Converts an IP from a signed decimal number (4 bytes) to dotted
     * notation
     *
     * @param ip the IP as a signed decimal number
     * @return the dotted-notation string
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
     * Converts an int value to a byte array representation
     * @param value the input value
     * @return the byte array
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
    * Converts a byte to an unsigned int
    * @param value the input byte
    * @return the unsigned int value
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
     * Converts a byte array to a decimal int value
     * @param bytes the byte array
     * @return the int value
     */
    public static int convertBytesToInt(byte[] bytes) {
        int result = 0;
        if (bytes != null && bytes.length == 4) {
            result = 0x000000ff & bytes[3] | 0x0000ff00 & (bytes[2] << 8) | 0x00ff0000 & (bytes[1] << 16) | 0xff000000 & (bytes[0] << 24);
        }

        return result;
    }

    /**
     * Converts a BASE64-encoded byte[] address to a human-readable string
     * @param base64Addr the BASE64-encoded address
     * @return the human-readable string
     */
    public static String base64ToString(String base64Addr) {
        if (Utils.isBlankString(base64Addr))
            return base64Addr;
        return IPUtils.convertIpToString(convertBytesToInt(Base64.getDecoder().decode(base64Addr)));
    }
}
