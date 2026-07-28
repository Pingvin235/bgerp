package ru.bgcrm.plugin.bgbilling.proto.model.inet.version.v8x;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetServiceType;

public class InetServiceType8x extends InetServiceType {


    private InetAddressType addressType8x;
    @Override
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
         * A specified range
         */
        RANGE(1),
        /**
         * A specified network
         */
        NET(2),
        /**
         * A specified address (IPv4)
         */
        SINGLE_IPV4(3),
        /**
         * A dynamic address
         */
        DYNAMIC(4),
        /**
         * A dynamic or specified address
         */
        DYNAMIC_OR_SINGLE(5),
        /**
         * A dynamic address or one from a range
         */
        DYNAMIC_OR_RANGE(6),
        /**
         * Either issue the specified address, or issue nothing
         */
        NONE_OR_SINGLE(7),
        /**
         * A static address (IPv6)
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