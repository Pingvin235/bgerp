package ru.bgcrm.util.inet;

import java.io.Serializable;
import java.math.BigInteger;

public class IpAddress implements Comparable<IpAddress>, Serializable {
    static final BigInteger mask4 = new BigInteger(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 });
    static final BigInteger mask16 = new BigInteger(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 });

    static final BigInteger two = BigInteger.valueOf(2);

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

    @Override
    public int compareTo(IpAddress o) {
        return IpAddress.compare(this.address, 0, this.address.length, o.address, 0, o.address.length);
    }

    /**
     * @param address
     * @return
     */
    public static BigInteger convertIp4AddressToBigInt(byte[] address) {
        /*
         * У BigInteger - есть хитрая особенность , он хранит отрицательные числа в дополнительном коде
         * И чтобы отличить положительное число, у которого в крайнем правом  левом бите стоит 0,  от отрициательного
         *	он добавляет еще один байт слева.
         */
        byte[] address2 = new byte[address[0] < 0 ? 5 : 4];
        System.arraycopy(address, 0, address2, address[0] < 0 ? 1 : 0, 4);

        return new BigInteger(address2);
    }

}
