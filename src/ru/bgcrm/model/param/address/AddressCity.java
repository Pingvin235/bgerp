package ru.bgcrm.model.param.address;

public class AddressCity
    extends AddressBase
{
    private int countryId = -1;
    private AddressCountry addressCountry; 

    public int getCountryId()
    {
        return countryId;
    }

    public void setCountryId( int countryId )
    {
        this.countryId = countryId;
    }

    public AddressCountry getAddressCountry()
    {
        return addressCountry;
    }

    public void setAddressCountry( AddressCountry addressCountry )
    {
        this.addressCountry = addressCountry;
    }
}
