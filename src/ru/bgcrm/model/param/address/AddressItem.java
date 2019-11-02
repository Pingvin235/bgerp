package ru.bgcrm.model.param.address;

/**
 * Улица, Квартал, Район - сущности, привязанные к городу.
 * @author Shamil
 */
public class AddressItem
    extends AddressBase
{
    private int cityId = -1;
    private AddressCity addressCity;

    public int getCityId()
    {
        return cityId;
    }

    public void setCityId( int cityId )
    {
        this.cityId = cityId;
    }

    public AddressCity getAddressCity()
    {
        return addressCity;
    }

    public void setAddressCity( AddressCity addressCity )
    {
        this.addressCity = addressCity;
    }
}
