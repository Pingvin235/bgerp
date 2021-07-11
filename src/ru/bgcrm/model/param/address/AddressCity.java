package ru.bgcrm.model.param.address;

public class AddressCity extends AddressBase {
    private int countryId = -1;
    private AddressCountry addressCountry;
    
    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int value) {
        this.countryId = value;
    }

    public AddressCity withCountryId(int value) {
        setCountryId(value);
        return this;
    }

    public AddressCountry getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(AddressCountry value) {
        this.addressCountry = value;
    }

    public AddressCity withTitle(String value) {
        setTitle(value);
        return this;
    }
}
