package ru.bgcrm.model.param.address;

public class AddressHouse extends AddressBase {
    public static final String OBJECT_TYPE = "address_house";

    private int areaId = -1;
    private int quarterId = -1;
    private int streetId = -1;
    /** House number only. */
    private int house = -1;
    /** Additional after number part: frac, letter. */
    private String frac;
    private String postIndex;
    private String comment = "";
    private AddressItem addressArea;
    private AddressItem addressQuarter;
    private AddressItem addressStreet;

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int value) {
        this.areaId = value;
    }

    public AddressHouse withAreaId(int value) {
        setAreaId(value);
        return this;
    }

    public int getQuarterId() {
        return quarterId;
    }

    public void setQuarterId(int value) {
        this.quarterId = value;
    }

    public AddressHouse withQuarterId(int value) {
        setQuarterId(value);
        return this;
    }

    public int getStreetId() {
        return streetId;
    }

    public void setStreetId(int value) {
        this.streetId = value;
    }

    public AddressHouse withStreetId(int value) {
        setStreetId(value);
        return this;
    }

    public int getHouse() {
        return house;
    }

    public void setHouse(int value) {
        this.house = value;
    }

    public String getFrac() {
        return frac;
    }

    public void setFrac(String value) {
        this.frac = value;
    }

    public String getHouseAndFrac() {
        StringBuilder buf = new StringBuilder();
        if (house > 0) {
            buf.append(house);
        }
        if (frac != null) {
            buf.append(frac);
        }
        return buf.toString();
    }

    public void setHouseAndFrac(String value) {
        if (value != null) {
            this.frac = "";
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                if (Character.isDigit(value.charAt(i))) {
                    str.append(value.charAt(i));
                } else {
                    this.frac = value.substring(i);
                    break;
                }
            }
            if (str.length() > 0) {
                this.house = Integer.parseInt(str.toString());
            }
        } else {
            this.house = 0;
            this.frac = null;
        }
    }

    public AddressHouse withHouseAndFrac(String value) {
        setHouseAndFrac(value);
        return this;
    }

    public String getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(String value) {
        this.postIndex = value;
    }

    public AddressHouse withPostIndex(String value) {
        setPostIndex(value);
        return this;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        this.comment = value;
    }

    public AddressHouse withComment(String value) {
        setComment(value);
        return this;
    }

    public AddressItem getAddressArea() {
        return addressArea;
    }

    public void setAddressArea(AddressItem value) {
        this.addressArea = value;
    }

    public AddressItem getAddressQuarter() {
        return addressQuarter;
    }

    public void setAddressQuarter(AddressItem value) {
        this.addressQuarter = value;
    }

    public AddressItem getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(AddressItem value) {
        this.addressStreet = value;
    }
}
