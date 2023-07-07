package ru.bgcrm.util.inet;

import java.math.BigInteger;

public class IpNet {
    protected byte[] subnet;
    protected int mask;

    public IpNet() {
    }

    public IpNet(byte[] subnet, int mask) {
        this.subnet = subnet;
        this.mask = mask;
    }

    public byte[] getSubnet() {
        return subnet;
    }

    public void setSubnet(byte[] subnet) {
        this.subnet = subnet;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    @Override
    public String toString() {
        return toString(subnet, mask);
    }

    /**
     * Возвращает строковое представление подсети.
     * @param subnet
     * @param mask
     * @return
     */
    public static String toString(final byte[] subnet, final int mask) {
        StringBuilder sb = new StringBuilder(subnet != null ? (subnet.length * 3 + 4) : 32);
        return sb.append(IpAddress.toString(subnet)).append('/').append(mask).toString();
    }

    /**
     * Возвращает строковое представление подсети.
     * @param subnet
     * @param mask
     * @return
     */
    public static String toString(final byte[] addressFrom, final byte[] addressTo) {
        int mask = addressTo != null ? getMask0(addressFrom, addressTo) : (addressFrom.length * 8);
        StringBuilder sb = new StringBuilder(addressFrom != null ? (addressFrom.length * 3 + 4) : 32);
        IpAddress.toString(addressFrom, sb);
        return sb.append('/').append(mask).toString();
    }

    public static int maskToInt(byte[] mask) {
        int result = 0;
        for (int i = mask.length - 1; i >= 0; i--) {
            int num = Integer.numberOfTrailingZeros(mask[i]);
            if (num >= 8) {
                result += 8;
            } else {
                result += num;
                break;
            }
        }

        return result;
    }

    public byte[] getMaxIp() {
        byte[] addressTo = new byte[subnet.length];
        System.arraycopy(subnet, 0, addressTo, 0, subnet.length);
        int i = subnet.length - 1;

        for (; i > mask / 8; i--) {
            addressTo[i] = (byte) 255;
        }

        if (mask % 8 != 0) {
            addressTo[i] |= (byte) (0xff >> (mask % 8));
        } else {
            if (mask / 8 == 4) {
                addressTo[i] |= (byte) (0xff >> 8);
            } else {
                addressTo[i] |= (byte) (0xff >> (mask % 8));
            }

        }

        return addressTo;
    }

    public static int getMask(byte[] addrFrom, byte[] addrTo) {
        BigInteger from = IpAddress.convertIp4AddresToBigInt(addrFrom);
        BigInteger to = IpAddress.convertIp4AddresToBigInt(addrTo);

        BigInteger addXor = from.xor(to);
        int n = 0;
        while (!addXor.equals(BigInteger.ZERO)) {
            addXor = addXor.shiftRight(1);
            n++;
        }
        return 32 - n;
    }

    private static int getMask0(final byte[] addressFrom, final byte[] addressTo) {
        assert addressFrom.length == addressTo.length;

        int size = addressFrom.length;
        for (int i = 0; i < size; i++) {
            if (addressFrom[i] != addressTo[i]) {
                return (i * 8) + (Integer.numberOfLeadingZeros((addressFrom[i] & 0xff) ^ (addressTo[i] & 0xff)) - (32 - 8));
            }
        }

        return size * 8;
    }

    /**
     * Создание новой подсети из правильного диапазона.
     * @param addressFrom
     * @param addressTo
     * @return
     */
    public static IpNet newInstance(final byte[] addressFrom, final byte[] addressTo) {
        int mask = addressTo != null ? getMask0(addressFrom, addressTo) : (addressFrom.length * 8);
        return new IpNet(addressFrom, mask);
    }

    public boolean inNet(final byte[] address) {
        return inNet(address, subnet, mask);
    }

    public static boolean inNet(final byte[] address, final byte[] subnet, final int mask) {
        if (mask == 0) {
            return true;
        }

        final int end = mask / 8;

        int i = end - 1;
        // если вдруг mask 1-7
        if (i < 0) {
            final int remainder = mask;
            if ((Integer.numberOfLeadingZeros((address[end] & 0xff) ^ (subnet[end] & 0xff)) - (32 - 8)) < (remainder)) {
                return false;
            }
        } else {
            // для быстрой проверки сверяем байт слева от последнего значащего
            if (address[i] != subnet[i]) {
                return false;
            }

            // если mask < 32
            if (end < subnet.length) {
                // проверяем последний значащий байт
                final int remainder = mask % 8;
                if (remainder > 0 && (Integer.numberOfLeadingZeros((address[end] & 0xff) ^ (subnet[end] & 0xff)) - (32 - 8)) < (remainder)) {
                    return false;
                }
            }

            // проверяем остальные байты слева
            for (--i; i >= 0; i--) {
                if (address[i] != subnet[i]) {
                    return false;
                }
            }
        }

        return true;
    }
}
