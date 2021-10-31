package ru.bgcrm.plugin.bgbilling.proto.model.inet.version.v8x;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceType;

public class InetServiceType8x extends InetServiceType {


    private InetAddressType addressType8x;

    public int getAddressType() {
        return addressType8x.code;
    }


    @JsonProperty("addressType")
    public void setaddressType8x(InetAddressType addressType) {
        this.addressType8x = addressType;
    }

    public InetAddressType getaddressType8X() {
        return addressType8x;
    }


    public enum InetAddressType {
        OFF(-1),
        NONE(0),
        /**
         * Указанный диапазон
         */
        RANGE(1),
        /**
         * Указанная сеть
         */
        NET(2),
        /**
         * Указанный адрес (IPv4)
         */
        SINGLE_IPV4(3),
        /**
         * Динамический адрес
         */
        DYNAMIC(4),
        /**
         * Динамический или указанный адрес
         */
        DYNAMIC_OR_SINGLE(5),
        /**
         * Динамический или из диапазона
         */
        DYNAMIC_OR_RANGE(6),
        /**
         * Либо выдавать указанный адрес, либо ничего не выдавать
         */
        NONE_OR_SINGLE(7),
        /**
         * Статический адрес (IPv6)
         */
        IPV6(8);

        private int code = 0;

        InetAddressType(int code) {
            this.code = code;
        }

        public static InetAddressType getInetAddressType(int code) {
            for (InetAddressType inetAddressType : values()) {
                if (inetAddressType.getCode() == code) {
                    return inetAddressType;
                }
            }
            return OFF;
        }

        public int getCode() {
            return code;
        }
    }


}