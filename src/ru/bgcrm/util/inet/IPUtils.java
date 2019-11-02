package ru.bgcrm.util.inet;

import java.math.BigInteger;
import java.util.Base64;
import java.util.StringTokenizer;

import ru.bgcrm.util.Utils;

public class IPUtils {
    /**
     * Преобразует строковое представление IP адреса с точками-разделителями в
     * десятичное число.
     * 
     * @param ip_value
     * @return IP адрес в виде десятичного числа либо 0L при ошибочном формате
     */
    public static final long convertStringIPtoLong(String ip_value) {
        return convertStringIPtoLong(ip_value, 0L);
    }

    /**
     * Преобразует строковое представление IP адреса с точками-разделителями в
     * десятичное число.
     * 
     * @param ip_value
     * @param error
     * @return IP адрес в виде десятичного числа либо error при ошибочном
     *         формате
     */
    public static final long convertStringIPtoLong(String ip_value, long error) {
        long val = 0L;

        try {
            StringTokenizer st = new StringTokenizer(ip_value, ".");
            if (st.hasMoreTokens())
                val |= (Long.parseLong(st.nextToken())) << 24;
            if (st.hasMoreTokens())
                val |= (Long.parseLong(st.nextToken())) << 16;
            if (st.hasMoreTokens())
                val |= (Long.parseLong(st.nextToken())) << 8;
            if (st.hasMoreTokens())
                val |= Long.parseLong(st.nextToken());

            val = val & 0xFFFFFFFFL;
        } catch (Exception ex) {
            val = error;
        }

        return val;
    }

    /**
     * Разбирает валидный только ip.
     * 
     * @param ipAddress
     *            строка-ip
     * @return значение IP
     */
    // TODO Вынесено из другого места, поглядеть как его обозвать и как может
    // совместить с существующими, которые могут разбирать и не только валидные
    // IP-адреса.
    public static long isIPaddress(String ipAddress) {
        if (ipAddress == null || ipAddress.length() == 0) {
            return 0L;
        }

        int pointNum = 0, numberNum = 0;
        int numberVal = 0;
        int len = ipAddress.length();
        int m = 0;
        long IPVal = 0;
        char tempchar;
        while (m < len && ((tempchar = ipAddress.charAt(m)) == '.' || Character.isDigit(tempchar))) {
            m++;
            if ((48 <= tempchar) && (tempchar <= 57)) {
                if (numberNum > 2)
                    return -1;
                numberVal *= 10;
                numberVal += tempchar - '0';
                if (numberVal > 255)
                    return -1;
                numberNum++;
            } else {
                if (numberNum == 0)
                    return -1;
                if (pointNum == 3)
                    return -1;
                pointNum++;
                IPVal *= 256;
                IPVal += numberVal;
                numberNum = 0;
                numberVal = 0;
            }
        }
        if (m != len || pointNum != 3 || numberNum == 0)
            return -1;
        pointNum++;
        IPVal *= 256;
        IPVal += numberVal;
        return IPVal;
    }

    /**
     * Преобразует представление IP адреса в виде десятичного числа в строковое
     * представление с точками-разделителями.
     * 
     * @param ip_value
     * @return
     */
    public static final String convertLongIpToString(long ip) {
        String net = "0.0.0.0";
        if (ip > 0) {
            long b1 = (ip & 0xff000000) >> 24;
            long b2 = (ip & 0x00ff0000) >> 16;
            long b3 = (ip & 0x0000ff00) >> 8;
            long b4 = ip & 0x000000ff;
            net = b1 + "." + b2 + "." + b3 + "." + b4;
        }
        return net;
    }

    /**
     * Преобразует строковое представление IP адреса с точками-разделителями в
     * десятичное число. При этом занимается бит знака.
     * 
     * @param ip_value
     * @param error
     * @return IP адрес в виде десятичного числа либо 0 при ошибочном формате
     */
    public static final int convertStringIPtoInt(String ip_value) {
        int result = 0;

        try {
            StringTokenizer st = new StringTokenizer(ip_value, ".");

            if (st.countTokens() != 4) {
                return 0;
            }

            result |= (Integer.parseInt(st.nextToken())) << 24;
            result |= (Integer.parseInt(st.nextToken())) << 16;
            result |= (Integer.parseInt(st.nextToken())) << 8;
            result |= Integer.parseInt(st.nextToken());
        } catch (Exception ex) {
            return 0;
        }

        return result;
    }

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
            byte[] b = Utils.convertIntToBytes(ip);
            net = Utils.unsignedByteToInt(b[0]) + "." + Utils.unsignedByteToInt(b[1]) + "."
                    + Utils.unsignedByteToInt(b[2]) + "." + Utils.unsignedByteToInt(b[3]);
        }
        return net;
    }

    /**
     * Преобразует маску и сеть в виде целых чисел без знака в диапазон IP
     * адресов
     * 
     * @param net
     *            адрес сети
     * @param mask
     *            маска сети
     * @return
     */
    public static final long[] netMaskToRange(long net, long mask) {
        long[] result = new long[2];

        result[0] = net;
        result[1] = net | (mask != 0 ? (0xffffffffL % mask) : 0xffffffffL);

        return result;
    }

    /**
     * Преобразует адрес и размер сетки в диапазон адресов.
     * 
     * @param address
     *            любой адрес сети
     * @param netSize
     *            размер адресной части сети в битах
     * @return
     */
    public static final long[] netMaskToRange(long address, int netSize) {
        long[] result = new long[2];

        long rawAddr = address;
        long temlate = netSize == 0 ? 0 : ((0xFFFFFFFFL << (32 - netSize)) & 0xFFFFFFFFL);
        result[0] = rawAddr & temlate;

        temlate = 0xFFFFFFFFL >> netSize;
        result[1] = rawAddr | temlate;

        return result;
    }

    /**
     * Возвращает маску сети заданной размерности
     * 
     * @param size
     * @return
     */
    public static final long getMask(int size) {
        long result = 0L;

        if (size <= 32) {
            for (int i = 0; i < size; i++) {
                result |= 1 << (31 - i);
            }
        }

        return result;
    }

    public static Netv4 subnet(long address1, long address2, short bitmask) {
        if (address1 <= 0 || address2 >= 0xffffffffL) {
            return null;
        }

        if (address1 > address2) {
            return null;
        }

        long network = address1 - 1;
        for (; network < address2; network++) {
            if ((network & (0xffffffffl >>> bitmask)) == 0)
                break;
        }

        if (network >= address2)
            return null;

        Netv4 netv4 = getNet(network + 1, bitmask);
        netv4.hostMax++;
        netv4.hostMin--;

        if (netv4.hostMax > address2)
            return null;
        else
            return netv4;

    }

    public static class Netv4 {
        public final long address;
        public final int bitmask;
        public final long netmask;
        public final long wildcard;
        public final long network;
        public long hostMin;
        public long hostMax;
        public final long broadcast;
        public final long hosts;

        Netv4(long address, short bitmask, long netmask, long wildcard, long network, long hostMin, long hostMax,
                long broadcast, long hosts) {
            super();
            this.address = address;
            this.bitmask = bitmask;
            this.netmask = netmask;
            this.wildcard = wildcard;
            this.network = network;
            this.hostMin = hostMin;
            this.hostMax = hostMax;
            this.broadcast = broadcast;
            this.hosts = hosts;
        }
    }

    public static Netv4 getNet(long address, short bitmask) {
        long netmask = (0xffffffffL << (32 - bitmask)) & 0xffffffffL;
        long wildcard = netmask ^ 0xffffffffL;
        long network = address & netmask;
        long broadcast = address | wildcard;
        long hostMin = network + 1;
        long hostMax = broadcast - 1;
        long hosts = hostMax - hostMin + 1;

        return new Netv4(address, bitmask, netmask, wildcard, network, hostMin, hostMax, broadcast, hosts);
    }

    /**
     * Проверка, является ли диапазон адресов IP сетью.
     * 
     * @param addrFrom
     * @param addrTo
     * @return
     */
    public static final boolean isRangeValidNet(byte[] addrFrom, byte[] addrTo) {
        if (addrFrom == null || addrTo == null || addrFrom.length != addrTo.length) {
            return false;
        }

        BigInteger aFrom = IpAddress.convertIp4AddresToBigInt(addrFrom);
        BigInteger aTo = IpAddress.convertIp4AddresToBigInt(addrTo);

        final int bits = addrFrom.length * 8;
        // признак того, что началась разница битов
        boolean diffStart = false;

        for (int i = bits - 1; i >= 0; i--) {
            boolean bitNetStart = aFrom.testBit(i);
            boolean bitNetEnd = aTo.testBit(i);

            // после окончания одинаковой зоны все биты в первом адресе должны
            // быть 0, во втором - 1
            if (diffStart) {
                if (bitNetStart || !bitNetEnd) {
                    return false;
                }
            } else {
                diffStart = bitNetStart != bitNetEnd;
            }
        }

        return true;
    }

    public static String getStringRange(IpAddress from, IpAddress to) {
        String address;
        if (from.equals(to)) {
            address = from.toString();
        } else {
            address = from.toString() + "-" + to.toString();
        }
        return address;
    }

    /**
     * Преобразует BASE64 кодированный byte[] адрес в человекочитаемою строку.
     * @param base64Addr
     * @return
     */
    public static String base64ToString(String base64Addr) {
        if (Utils.isBlankString(base64Addr))
            return base64Addr;
        return IPUtils.convertIpToString(Utils.convertBytesToInt(Base64.getDecoder().decode(base64Addr)));
    }
    
    /**
     * Преобразует человекочитаемый IP адрес в BASE64 кодированный byte[].
     * @param addr
     * @return
     */
    public static String stringToBase64(String addr) {
        if (Utils.isBlankString(addr))
            return addr;
        return Base64.getEncoder().encodeToString(Utils.convertIntToBytes(IPUtils.convertStringIPtoInt(addr)));
    }
}
