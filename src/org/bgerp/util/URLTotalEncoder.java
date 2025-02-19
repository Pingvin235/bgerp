package org.bgerp.util;

import java.nio.charset.Charset;

import ru.bgcrm.util.Utils;

/**
 * Encodes all bytes to {@code %} with hex digits.
 *
 * @author Shamil Vakhitov
 */
public class URLTotalEncoder {
    private URLTotalEncoder() { }

    public static String encode(String value, Charset charset) {
        byte[] bytes = value.getBytes(charset);

        StringBuilder result = new StringBuilder(bytes.length * 3);

        for (int i = 0; i < bytes.length; i++)
            result
                .append("%")
                .append(Utils.HEX[(bytes[i] & 0xF0) >> 4])
                .append(Utils.HEX[bytes[i] & 0x0F]);

        return result.toString();
    }
}
