package ru.bgcrm.util.inet;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;

public class IpAddress implements Comparable<IpAddress>, Serializable {
    static final BigInteger mask4 = new BigInteger(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 });
    static final BigInteger mask16 = new BigInteger(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 });

    static final BigInteger two = BigInteger.valueOf(2);

    private static final byte[] nullBytes = new byte[24];

    public static final IpAddress ZERO_ADDRESS = new IpAddress(new byte[] { 0, 0, 0, 0 });
    public static final IpAddress IPV6_ZERO_ADDRESS = new IpAddress(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

    public byte[] address;

    public IpAddress() {
    }

    public IpAddress(byte[] address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        return hashCode(address);
    }

    public static int hashCode(final byte a[]) {
        if (a == null) {
            return 0;
        }

        int result = 1;

        for (int i = 0; i < a.length; i++) {
            result = 31 * result + a[i];
        }

        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        final byte[] a = address;
        final byte[] a2 = ((IpAddress) obj).address;

        return equals(a, a2);
    }

    public static boolean equals(byte[] a, byte[] a2) {
        if (a == a2) {
            return true;
        } else if (a == null || a2 == null) {
            return false;
        }

        int length = a.length;
        if (a2.length != length) {
            return false;
        }

        // более вероятно, что чаще адреса будут расходиться в конце, поэтому сравнивать на совпадение лучше с конца.
        for (int i = length - 1; i >= 0; i--) {
            if (a[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }

    public void clear() {
        System.arraycopy(nullBytes, 0, address, 0, address.length);
    }

    @Override
    public String toString() {
        return toString(address);
    }

    public static String toString(final byte[] address) {
        StringBuilder sb = new StringBuilder(64);
        toString(address, sb);
        return sb.toString();
    }

    public static void toString(final byte[] address, StringBuilder sb) {
        if (address == null) {
            sb.append("[IpAddress:null]");
            return;
        }

        if (address.length == 4) {
            sb.append(address[0] & 0xff).append('.').append(address[1] & 0xff).append('.').append(address[2] & 0xff).append('.')
                    .append(address[3] & 0xff);
            return;
        } else {
            for (int i = 0, size = address.length; i < size; i += 2) {
                int k = (address[i + 1] & 255) | (address[i] << 8 & 0xff00);
                sb.append(Integer.toHexString(k));
                sb.append(":");
            }

            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            return;
        }
    }

    public static final Comparator<byte[]> comparator = new Comparator<>() {
        @Override
        public int compare(byte[] o1, byte[] o2) {
            return IpAddress.compare(o1, 0, o1.length, o2, 0, o2.length);
        }
    };

    public static final int compare(final byte[] v1, final byte[] v2) {
        int k = 0;
        int lim = v1.length;
        while (k < lim) {
            short c1 = ((short) (v1[k] & 0xff));
            short c2 = ((short) (v2[k] & 0xff));
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }

        return 0;
    }

    public static final int compare(final byte[] v1, int i, final int len1, final byte[] v2, int j, final int len2) {
        int n = Math.min(len1, len2);

        if (i == j) {
            int k = i;
            int lim = n + i;
            while (k < lim) {
                short c1 = ((short) (v1[k] & 0xff));
                short c2 = ((short) (v2[k] & 0xff));
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
        } else {
            while (n-- != 0) {
                short c1 = ((short) (v1[i++] & 0xff));
                short c2 = ((short) (v2[j++] & 0xff));
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
        }

        return len1 - len2;
    }

    public static byte[] newAndIncrement(final byte[] ip) {
        byte[] result = new byte[ip.length];
        System.arraycopy(ip, 0, result, 0, ip.length);

        if (increment(result)) {
            return result;
        } else {
            return null;
        }
    }

    public static boolean increment(final byte[] ip) {
        return increment(ip, 0);
    }

    public static boolean increment(final byte[] ip, int fromBit) {
        int pos = (ip.length - 1) - (fromBit / 8);
        int bit = fromBit % 8;

        if (bit > 0) {
            int v = ((short) (ip[pos] & 0xff)) + (1 << bit);
            if (v <= 255) {
                ip[pos] = ((byte) ((v + 1) & 0xff));
                return true;
            } else {
                ip[pos--] = ((byte) (0 & 0xff));
            }
        }

        for (int i = pos; i >= 0; i--) {
            short v = ((short) (ip[i] & 0xff));
            if (v < 255) {
                ip[i] = ((byte) ((v + 1) & 0xff));
                return true;
            } else {
                ip[i] = ((byte) (0 & 0xff));
            }
        }

        return false;
    }

    public static byte[] newAndDecrement(final byte[] ip) {
        byte[] result = new byte[ip.length];
        System.arraycopy(ip, 0, result, 0, ip.length);

        for (int i = ip.length - 1; i >= 0; i--) {
            short v = ((short) (result[i] & 0xff));
            if (v > 0) {
                result[i] = ((byte) ((v - 1) & 0xff));
                return result;
            } else {
                result[i] = ((byte) (255 & 0xff));
            }
        }

        return null;
    }

    @Override
    public int compareTo(IpAddress o) {
        return IpAddress.compare(this.address, 0, this.address.length, o.address, 0, o.address.length);
    }

    @Override
    public IpAddress clone() {
        byte[] addr = new byte[this.address.length];
        System.arraycopy(this.address, 0, addr, 0, addr.length);

        return new IpAddress(addr);
    }

    /**
     * @deprecated - use {@link IpResourceRange#intersects(byte[], byte[], byte[], byte[])}
     * функция пересечения отрезков. Все параметры не пустые , иначе  NullPointerException
     * @param addressFrom
     * @param addressTo
     * @param addressFrom2
     * @param addressTo2
     * @return
     */
    @Deprecated
    public static boolean instersect(byte[] addressFrom, byte[] addressTo, byte[] addressFrom2, byte[] addressTo2) {
        if (addressFrom == null || addressTo == null || addressFrom2 == null || addressTo2 == null) {
            throw new NullPointerException();
        }

        return compare(addressFrom, addressTo2) <= 0 && compare(addressTo, addressFrom2) >= 0;

    }

    /**
     * @deprecated - use {@link IpResourceRange#inRange(byte[], byte[], byte[], byte[])}
     * функция вхождения одного отрезка в другой. Все параметры не пустые , иначе  NullPointerException.
     * @param addressFrom
     * @param addressTo
     * @param addressFrom2
     * @param addressTo2
     * @return
     */
    @Deprecated
    public static boolean rangeInRange(byte[] addressFrom, byte[] addressTo, byte[] addressFrom2, byte[] addressTo2) {
        if (addressFrom == null || addressTo == null || addressFrom2 == null || addressTo2 == null) {
            throw new NullPointerException();
        }

        return IpAddress.compare(addressFrom, addressFrom2) <= 0 && IpAddress.compare(addressTo, addressTo2) >= 0;
    }

    /**
     * @deprecated - use {@link IpResourceRange#inRange(byte[], byte[], byte[], byte[])}
     * @param ip
     * @param addressFrom
     * @param addressTo
     * @return
     */
    @Deprecated
    public static boolean ipInRange(byte[] ip, byte[] addressFrom, byte[] addressTo) {
        return compare(addressFrom, ip) <= 0 && compare(addressTo, ip) >= 0;
    }

    /**
     * @param address
     * @return
     */
    public static BigInteger convertIp4AddresToBigInt(byte[] address) {
        /*
         * У BigInteger - есть хитрая особенность , он хранит отрицательные числа в дополнительном коде
         * И чтобы отличить положительное число, у которого в крайнем правом  левом бите стоит 0,  от отрициательного
         *	он добавляет еще один байт слева.
         */
        byte[] address2 = new byte[address[0] < 0 ? 5 : 4];
        System.arraycopy(address, 0, address2, address[0] < 0 ? 1 : 0, 4);

        return new BigInteger(address2);
    }

    /**
     * @param bigInteger
     * @return
     */
    public static byte[] convertBigIntToIp4Address(BigInteger bigInteger) {
        byte[] address = bigInteger.toByteArray();

        /*
         *  У BigInteger - есть хитрая особенность , он хранит отрицательные числа в дополнительном коде
         * И чтобы отличить положительное число, у которого в крайнем правом  левом бите стоит 0,  от отрициательного
         * он добавляет еще один байт слева.
         */
        byte[] address2 = { 0, 0, 0, 0 };
        System.arraycopy(address, address.length == 5 ? 1 : 0, address2, address.length == 5 ? 0 : (4 - address.length),
                address.length == 5 ? 4 : address.length);

        return address2;
    }

    /**
     * @deprecated - use {@link IpResourceRange#toString(IpAddress, IpAddress)}
     * Форматирует диапазон адресов к виду от-до.
     * @param from
     * @param to
     * @return
     */
    @Deprecated
    public static String formatRange(IpAddress from, IpAddress to) {
        StringBuilder result = new StringBuilder(50);

        if (to == null) {
            result.append(from);
        } else {
            result.append(from);
            result.append("-");
            result.append(to);
        }

        return result.toString();
    }

    /**
     * @deprecated - use {@link IpResourceRange#toString(byte[], byte[])}
     * Форматирует диапазон адресов к виду от-до.
     * @param from
     * @param to
     * @return
     */
    @Deprecated
    public static String formatRange(byte[] from, byte[] to) {
        StringBuilder result = new StringBuilder(50);

        if (to == null) {
            result.append(toString(from));
        } else {
            result.append(toString(from));
            result.append("-");
            result.append(toString(to));
        }

        return result.toString();
    }
}
