package ru.bgcrm.model.param.address;

/**
 * Street, area or quarter.
 * 
 * @author Shamil Vakhitov
 */
public class AddressItem extends AddressBase {
    private int cityId = -1;
    private AddressCity addressCity;

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int value) {
        this.cityId = value;
    }

    public AddressItem withCityId(int value) {
        setCityId(value);
        return this;
    }

    public AddressCity getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(AddressCity value) {
        this.addressCity = value;
    }

    public AddressItem withTitle(String value) {
        setTitle(value);
        return this;
    }
}
