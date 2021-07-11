package org.bgerp.itest.kernel.param;

import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressCountry;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;

@Test(groups = "address", dependsOnGroups = "config")
public class AddressTest {
    public static volatile AddressHouse houseMuenchen;
    public static volatile AddressHouse houseUfa7f1;
    public static volatile AddressHouse houseUfa6;

    @Test
    public void addConfig() throws Exception {
        // TODO: Add different format types.
    }

    @Test
    public void addAddressDirectory() throws Exception {
        var dao = new AddressDAO(DbTest.conRoot);

        var country = dao.updateAddressCountry(new AddressCountry().withTitle("Bayern"));
        var city = dao.updateAddressCity(new AddressCity().withCountryId(country.getId()).withTitle("München"));
        var area = dao.updateAddressArea(new AddressItem().withCityId(city.getId()).withTitle("Obermenzing"));
        var street = dao.updateAddressStreet(new AddressItem().withCityId(city.getId()).withTitle("Karl-Marx-Ring"));
        street = dao.updateAddressStreet(new AddressItem().withCityId(city.getId()).withTitle("Dorfstraße"));
        houseMuenchen = dao.updateAddressHouse(new AddressHouse().withStreetId(street.getId()).withAreaId(area.getId())
            .withPostIndex("81247").withHouseAndFrac("99a").withComment("Nette Leute"));
        Assert.assertTrue(houseMuenchen.getId() > 0);

        country = dao.updateAddressCountry(new AddressCountry().withTitle("Башкортостан"));
        city = dao.updateAddressCity(new AddressCity().withCountryId(country.getId()).withTitle("Уфа"));
        area =  dao.updateAddressArea(new AddressItem().withCityId(city.getId()).withTitle("Кировский район"));
        var quarter = dao.updateAddressQuarter(new AddressItem().withCityId(city.getId()).withTitle("33"));
        street = dao.updateAddressStreet(new AddressItem().withCityId(city.getId()).withTitle("Карла Маркса"));
        street = dao.updateAddressStreet(new AddressItem().withCityId(city.getId()).withTitle("Габдуллы Амантая"));
        houseUfa7f1 = dao.updateAddressHouse(new AddressHouse().withStreetId(street.getId())
            .withAreaId(area.getId()).withQuarterId(quarter.getId())
            .withPostIndex("450103").withHouseAndFrac("7/1").withComment("Код домофона: 666"));
        houseUfa6 = dao.updateAddressHouse(new AddressHouse().withStreetId(street.getId())
            .withAreaId(area.getId()).withQuarterId(quarter.getId())
            .withPostIndex("450103").withHouseAndFrac("6").withComment("Чокнутая консьержка"));
        Assert.assertTrue(houseUfa7f1.getId() > 0 );
    }
}
