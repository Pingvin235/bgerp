package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.dao.param.Tables;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressCountry;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.util.Utils;

public class AddressDAO extends CommonDAO {
    public static final int LOAD_LEVEL_HOUSE = 1;
    public static final int LOAD_LEVEL_STREET = 2;
    public static final int LOAD_LEVEL_CITY = 3;
    public static final int LOAD_LEVEL_COUNTRY = 4;

    private static final int getHouseLoadLevel(boolean loadCountryData, boolean loadCityData, boolean loadStreetData) {
        if (loadCountryData) {
            return LOAD_LEVEL_COUNTRY;
        }
        if (loadCityData) {
            return LOAD_LEVEL_CITY;
        }
        if (loadStreetData) {
            return LOAD_LEVEL_STREET;
        }
        return 0;
    }

    public AddressDAO(Connection con) {
        super(con);
    }

    public void searchAddressCountryList(Pageable<AddressCountry> searchResult, String title) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<AddressCountry> result = searchResult.getList();

            PreparedQuery ps = new PreparedQuery(con);
            ps.addQuery("SELECT SQL_CALC_FOUND_ROWS * ");
            ps.addQuery("FROM ");
            ps.addQuery(Tables.TABLE_ADDRESS_COUNTRY);

            if (!Utils.isEmptyString(title)) {
                ps.addQuery("WHERE ");
                ps.addQuery("title LIKE ? ");
                ps.addString(title);
            }

            ps.addQuery(" ORDER BY title");
            ps.addQuery(getPageLimit(page));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.add(getAddressCountryFromRs(rs, ""));
            }

            if (page != null) {
                page.setRecordCount(ps.getPrepared());
            }

            ps.close();
        }
    }

    public AddressCountry getAddressCountry(int id) throws SQLException {
        AddressCountry addressCountry = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(Tables.TABLE_ADDRESS_COUNTRY);
        query.append(" WHERE id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            addressCountry = getAddressCountryFromRs(rs, "");
        }

        ps.close();

        return addressCountry;
    }

    public void searchAddressCityList(Pageable<AddressCity> searchResult, int countryId, String title, boolean loadCountryData,
            Set<Integer> cityIdFilter) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<AddressCity> result = searchResult.getList();

            PreparedQuery ps = new PreparedQuery(con);
            ps.addQuery("SELECT SQL_CALC_FOUND_ROWS * ");
            ps.addQuery("FROM ");
            ps.addQuery(Tables.TABLE_ADDRESS_CITY);
            ps.addQuery("AS city ");

            if (loadCountryData) {
                ps.addQuery("LEFT JOIN ");
                ps.addQuery(Tables.TABLE_ADDRESS_COUNTRY);
                ps.addQuery("AS country ON city.country_id = country.id ");
            }

            ps.addQuery("WHERE 1=1");

            if (!Utils.isEmptyString(title)) {
                ps.addQuery(" AND city.title LIKE ? ");
                ps.addString(title);
            }

            if (countryId > 0) {
                ps.addQuery(" AND city.country_id = ? ");
                ps.addInt(countryId);
            }

            if (cityIdFilter != null && cityIdFilter.size() > 0) {
                ps.addQuery(" AND city.id IN ( ");
                ps.addQuery(Utils.toString(cityIdFilter, "", ","));
                ps.addQuery(" ) ");
            }

            ps.addQuery(" ORDER BY city.title");
            ps.addQuery(getPageLimit(page));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AddressCity addressCity = getAddressCityFromRs(rs, "city.");
                if (loadCountryData) {
                    addressCity.setAddressCountry(getAddressCountryFromRs(rs, "country."));
                }
                result.add(addressCity);
            }

            page.setRecordCount(ps.getPrepared());

            ps.close();
        }
    }

    public AddressCity getAddressCity(int id, boolean loadCountryData) throws SQLException {
        AddressCity addressCity = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(Tables.TABLE_ADDRESS_CITY);
        query.append(" AS city");
        if (loadCountryData) {
            query.append(" LEFT JOIN ");
            query.append(Tables.TABLE_ADDRESS_COUNTRY);
            query.append(" AS country ON city.country_id=country.id");
        }
        query.append(" WHERE city.id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            addressCity = getAddressCityFromRs(rs, "city.");
            if (loadCountryData) {
                addressCity.setAddressCountry(getAddressCountryFromRs(rs, "country."));
            }
        }
        ps.close();

        return addressCity;
    }

    /**
     * Selects cities restricted and ordered by their IDs
     * @param ids the IDs
     * @return
     * @throws SQLException
     */
    public List<IdTitle> getAddressCities(List<Integer> ids) throws SQLException {
        var result = new ArrayList<IdTitle>();

        String idsStr = Utils.toString(ids);

        String query = SQL_SELECT_ALL_FROM + Tables.TABLE_ADDRESS_CITY
            + SQL_WHERE + "id IN (" + idsStr + ")"
            + SQL_ORDER_BY + "FIELD (id," + idsStr + ")";
        try (var ps = con.prepareStatement(query)) {
            var rs = ps.executeQuery();
            while (rs.next())
                result.add(new IdTitle(rs.getInt("id"), rs.getString("title")));
        }

        return result;
    }

    public void searchAddressAreaList(Pageable<AddressItem> searchResult, int cityId) throws SQLException {
        searchAddressItemList(Tables.TABLE_ADDRESS_AREA, searchResult, Collections.singleton(cityId), null, false, false);
    }

    public void searchAddressAreaList(Pageable<AddressItem> searchResult, int cityId, List<String> title, boolean loadCountryData, boolean loadCityData)
            throws SQLException {
        searchAddressItemList(Tables.TABLE_ADDRESS_AREA, searchResult, Collections.singleton(cityId), title, loadCountryData, loadCityData);
    }

    public AddressItem getAddressArea(int id, boolean loadCountryData, boolean loadCityData) throws SQLException {
        return getAddressItem(Tables.TABLE_ADDRESS_AREA, id, loadCountryData, loadCityData);
    }

    public void searchAddressQuarterList(Pageable<AddressItem> searchResult, int cityId) throws SQLException {
        searchAddressItemList(Tables.TABLE_ADDRESS_QUARTER, searchResult, Collections.singleton(cityId), null, false, false);
    }

    public void searchAddressQuarterList(Pageable<AddressItem> searchResult, int cityId, List<String> title, boolean loadCountryData,
            boolean loadCityData) throws SQLException {
        searchAddressItemList(Tables.TABLE_ADDRESS_QUARTER, searchResult, Collections.singleton(cityId), title, loadCountryData, loadCityData);
    }

    public AddressItem getAddressQuarter(int id, boolean loadCountryData, boolean loadCityData) throws SQLException {
        return getAddressItem(Tables.TABLE_ADDRESS_QUARTER, id, loadCountryData, loadCityData);
    }

    public void searchAddressStreetList(Pageable<AddressItem> searchResult, int cityId) throws SQLException {
        searchAddressItemList(Tables.TABLE_ADDRESS_STREET, searchResult, Collections.singleton(cityId), null, false, false);
    }

    public void searchAddressStreetList(Pageable<AddressItem> searchResult, Set<Integer> cityIds, List<String> title, boolean loadCountryData,
            boolean loadCityData) throws SQLException {
        searchAddressItemList(Tables.TABLE_ADDRESS_STREET, searchResult, cityIds, title, loadCountryData, loadCityData);
    }

    public AddressItem getAddressStreet(int id, boolean loadCountryData, boolean loadCityData) throws SQLException {
        return getAddressItem(Tables.TABLE_ADDRESS_STREET, id, loadCountryData, loadCityData);
    }

    /**
     * Searches address item.
     * @param tableName table name.
     * @param searchResult result.
     * @param cityIds optional city IDs.
     * @param title optional search by titles, set with LIKE masks; a single entity must match to item title; for many tokens each of that has to match concatenated titles.
     * @param loadCountryData load countries.
     * @param loadCityData load cities.
     * @throws SQLException
     */
    private void searchAddressItemList(String tableName, Pageable<AddressItem> searchResult, Set<Integer> cityIds, List<String> title,
            boolean loadCountryData, boolean loadCityData) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<AddressItem> result = searchResult.getList();

            PreparedQuery pq = new PreparedQuery(con, SQL_SELECT_COUNT_ROWS + "*");

            if (title != null && title.size() > 1) {
                pq.addQuery(", CONCAT(item.title");
                if (loadCityData)
                    pq.addQuery(", ' ', city.title");
                pq.addQuery(") AS titles ");
            }

            pq.addQuery(SQL_FROM);
            pq.addQuery(tableName);
            pq.addQuery(" AS item ");

            if (loadCityData) {
                pq.addQuery("LEFT JOIN ");
                pq.addQuery(Tables.TABLE_ADDRESS_CITY);
                pq.addQuery(" AS city ON item.city_id = city.id ");

                if (loadCountryData) {
                    pq.addQuery("LEFT JOIN ");
                    pq.addQuery(Tables.TABLE_ADDRESS_COUNTRY);
                    pq.addQuery(" AS country ON city.country_id = country.id ");
                }
            }

            pq.addQuery("WHERE 1=1 ");

            if (CollectionUtils.isNotEmpty(cityIds)) {
                boolean hasPositiveCityIds = false;

                Iterator<Integer> iterator = cityIds.iterator();
                while (!hasPositiveCityIds && iterator.hasNext()) {
                    Integer cityId = iterator.next();
                    if (cityId > 0) {
                        hasPositiveCityIds = true;
                        pq.addQuery(" AND city_id IN( " + Utils.toString(cityIds) + " ) ");
                    }
                }
            }

            if (title != null) {
                if (title.size() == 1) {
                    pq.addQuery(" AND item.title LIKE ? ");
                    pq.addString(title.get(0));
                } else if (title.size() > 1) {
                    pq.addQuery(" HAVING titles LIKE ?");
                    pq.addString(title.get(0));

                    for (int i = 1; i < title.size(); i++) {
                        pq.addQuery(" AND titles LIKE ?");
                        pq.addString(title.get(1));
                    }
                }
            }

            pq.addQuery(SQL_ORDER_BY);
            if (loadCityData)
                pq.addQuery("city.title, ");
            pq.addQuery("item.title ");
            pq.addQuery(getPageLimit(page));

            ResultSet rs = pq.executeQuery();

            while (rs.next()) {
                AddressItem addressItem = getAddressItemFromRs(rs, "item.");
                if (loadCityData) {
                    AddressCity addressCity = getAddressCityFromRs(rs, "city.");
                    addressItem.setAddressCity(addressCity);
                    if (loadCountryData && addressCity != null) {
                        AddressCountry addressCountry = getAddressCountryFromRs(rs, "country.");
                        addressCity.setAddressCountry(addressCountry);
                    }
                }
                result.add(addressItem);
            }

            if (page != null) {
                page.setRecordCount(pq.getPrepared());
            }

            pq.close();
        }
    }

    private AddressItem getAddressItem(String tableName, int id, boolean loadCountryData, boolean loadCityData) throws SQLException {
        AddressItem addressItem = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM ");
        query.append(tableName);
        query.append(" AS item");
        if (loadCityData) {
            query.append(" LEFT JOIN ");
            query.append(Tables.TABLE_ADDRESS_CITY);
            query.append(" AS city ON item.city_id=city.id");
            if (loadCountryData) {
                query.append(" LEFT JOIN ");
                query.append(Tables.TABLE_ADDRESS_COUNTRY);
                query.append(" AS country ON city.country_id=country.id");
            }
        }
        query.append(" WHERE item.id=?");
        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            addressItem = getAddressItemFromRs(rs, "item.");
            if (loadCityData) {
                AddressCity addressCity = getAddressCityFromRs(rs, "city.");
                addressItem.setAddressCity(addressCity);
                if (loadCountryData && addressCity != null) {
                    AddressCountry addressCountry = getAddressCountryFromRs(rs, "country.");
                    addressCity.setAddressCountry(addressCountry);
                }
            }
        }
        ps.close();

        return addressItem;
    }

    public void searchAddressHouseList(Pageable<AddressHouse> searchResult, int streetId, String housePrefix) throws SQLException {
        searchAddressHouseList(searchResult, streetId, housePrefix, false, false, false, false);
    }

    public void searchAddressHouseList(Pageable<AddressHouse> searchResult, int streetId, String house, boolean absolute, boolean loadCountryData,
            boolean loadCityData, boolean loadStreetData) throws SQLException {
        if (searchResult != null && streetId > 0) {
            Page page = searchResult.getPage();
            List<AddressHouse> result = searchResult.getList();

            PreparedQuery pq = new PreparedQuery(con);

            AddressHouse searchParams = new AddressHouse().withHouseAndFrac(house);

            int number = searchParams.getHouse();
            String frac = searchParams.getFrac();

            int loadLevel = getHouseLoadLevel(loadCountryData, loadCityData, loadStreetData);

            pq.addQuery("SELECT SQL_CALC_FOUND_ROWS * FROM ");
            pq.addQuery(Tables.TABLE_ADDRESS_HOUSE);
            pq.addQuery(" AS house");
            addHouseSelectQueryJoins(pq.getQuery(), loadLevel);

            pq.addQuery(" WHERE 1=1 AND house.street_id=?");
            pq.addInt(streetId);

            if (absolute) {
                if (number > 0) {
                    pq.addQuery(" AND house.house=? ");
                    pq.addInt(number);

                    if (frac != null) {
                        pq.addQuery(" AND house.frac=?");
                        pq.addString(frac);
                    }
                }
            } else {
                if (number > 0) {
                    pq.addQuery(" AND house.house LIKE CONCAT(?, '%')");
                    pq.addInt(number);
                }
                if (Utils.notBlankString(frac)) {
                    pq.addQuery(" AND house.frac LIKE CONCAT('%', ?, '%')");
                    pq.addString(frac);
                }
            }

            pq.addQuery(" ORDER BY house.house, house.frac");
            pq.addQuery(getPageLimit(page));

            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                result.add(getAddressHouseFromRs(rs, "house.", loadLevel));
            }
            if (page != null) {
                page.setRecordCount(pq.getPrepared());
            }
            pq.close();
        }
    }

    public AddressHouse getAddressHouse(int id, boolean loadCountryData, boolean loadCityData, boolean loadStreetData) throws SQLException {
        AddressHouse addressHouse = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();

        int loadLevel = getHouseLoadLevel(loadCountryData, loadCityData, loadStreetData);

        query.append("SELECT * FROM ");
        query.append(Tables.TABLE_ADDRESS_HOUSE);
        query.append(" AS house");
        addHouseSelectQueryJoins(query, loadLevel);
        query.append(" WHERE house.id=?");
        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            addressHouse = getAddressHouseFromRs(rs, "house.", loadLevel);

        }
        ps.close();

        return addressHouse;
    }

    public static void addHouseSelectQueryJoins(StringBuilder query, int loadLevel) {
        if (loadLevel >= LOAD_LEVEL_STREET) {
            query.append(" LEFT JOIN ");
            query.append(Tables.TABLE_ADDRESS_STREET);
            query.append(" AS street ON house.street_id=street.id");
            query.append(" LEFT JOIN ");
            query.append(Tables.TABLE_ADDRESS_AREA);
            query.append(" AS area ON house.area_id=area.id");
            query.append(" LEFT JOIN ");
            query.append(Tables.TABLE_ADDRESS_QUARTER);
            query.append(" AS quarter ON house.quarter_id=quarter.id");

            if (loadLevel >= LOAD_LEVEL_CITY) {
                query.append(" LEFT JOIN ");
                query.append(Tables.TABLE_ADDRESS_CITY);
                query.append(" AS city ON street.city_id=city.id");

                if (loadLevel >= LOAD_LEVEL_COUNTRY) {
                    query.append(" LEFT JOIN ");
                    query.append(Tables.TABLE_ADDRESS_COUNTRY);
                    query.append(" AS country ON city.country_id=country.id");
                }
            }
        }
    }

    public AddressCountry updateAddressCountry(AddressCountry value) throws SQLException {
        if (value != null) {
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();

            if (value.getId() <= 0) {
                query.append("INSERT INTO ");
                query.append(Tables.TABLE_ADDRESS_COUNTRY);
                query.append(" SET title=?, last_update = NOW()");
                ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, value.getTitle());
                ps.executeUpdate();
                value.setId(lastInsertId(ps));
                ps.close();
            } else {
                query.append("UPDATE ");
                query.append(Tables.TABLE_ADDRESS_COUNTRY);
                query.append(" SET title=?, last_update = NOW() WHERE id=?");
                ps = con.prepareStatement(query.toString());
                ps.setString(1, value.getTitle());
                ps.setInt(2, value.getId());
                ps.executeUpdate();
                ps.close();
            }
        }
        return value;
    }

    public void deleteAddressCountry(int id) throws SQLException, BGMessageException {
        StringBuilder query = new StringBuilder();

        query.append("SELECT COUNT(*) FROM ");
        query.append(Tables.TABLE_ADDRESS_CITY);
        query.append("WHERE country_id=?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            throw new BGMessageException("Страна используется в городах.");
        }
        ps.close();

        query.setLength(0);
        query.append(SQL_DELETE_FROM);
        query.append(Tables.TABLE_ADDRESS_COUNTRY);
        query.append("WHERE id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public AddressCity updateAddressCity(AddressCity city) throws SQLException {
        if (city != null) {
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();

            if (city.getId() <= 0) {
                query.append("INSERT INTO ");
                query.append(Tables.TABLE_ADDRESS_CITY);
                query.append(" SET country_id=?, title=?, last_update = NOW()");
                ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, city.getCountryId());
                ps.setString(2, city.getTitle());
                ps.executeUpdate();
                city.setId(lastInsertId(ps));
                ps.close();
            } else {
                query.append("UPDATE ");
                query.append(Tables.TABLE_ADDRESS_CITY);
                query.append(" SET title=?, last_update = NOW() WHERE id=?");
                ps = con.prepareStatement(query.toString());
                ps.setString(1, city.getTitle());
                ps.setInt(2, city.getId());
                ps.executeUpdate();
                ps.close();
            }
        }
        return city;
    }

    public void deleteAddressCity(int id) throws SQLException, BGMessageException {
        checkItem(id, Tables.TABLE_ADDRESS_STREET, "улицах");
        checkItem(id, Tables.TABLE_ADDRESS_QUARTER, "кварталах");
        checkItem(id, Tables.TABLE_ADDRESS_AREA, "районах");

        StringBuilder query = new StringBuilder();
        query.append(SQL_DELETE_FROM);
        query.append(Tables.TABLE_ADDRESS_CITY);
        query.append("WHERE id=?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private void checkItem(int id, String table, String title) throws SQLException, BGMessageException {
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(*) FROM ");
        query.append(table);
        query.append("WHERE city_id=?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            throw new BGMessageException("Город используется в " + title + ".");
        }
        ps.close();
    }

    public AddressItem updateAddressArea(AddressItem item) throws SQLException {
        return updateAddressItem(Tables.TABLE_ADDRESS_AREA, item);
    }

    public AddressItem updateAddressQuarter(AddressItem addressItem) throws SQLException {
        return updateAddressItem(Tables.TABLE_ADDRESS_QUARTER, addressItem);
    }

    public AddressItem updateAddressStreet(AddressItem addressItem) throws SQLException {
        return updateAddressItem(Tables.TABLE_ADDRESS_STREET, addressItem);
    }

    public void deleteAddressArea(int id) throws SQLException, BGMessageException {
        deleteAddressItem(Tables.TABLE_ADDRESS_AREA, "area_id", id);
    }

    public void deleteAddressQuarter(int id) throws SQLException, BGMessageException {
        deleteAddressItem(Tables.TABLE_ADDRESS_QUARTER, "quarter_id", id);
    }

    public void deleteAddressStreet(int id) throws SQLException, BGMessageException {
        deleteAddressItem(Tables.TABLE_ADDRESS_STREET, "street_id", id);
    }

    private AddressItem updateAddressItem(String tableName, AddressItem addressItem) throws SQLException {
        if (addressItem != null) {
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();

            if (addressItem.getId() <= 0) {
                query.append("INSERT INTO ");
                query.append(tableName);
                query.append(" SET city_id=?, title=?, last_update = NOW()");
                ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, addressItem.getCityId());
                ps.setString(2, addressItem.getTitle());
                ps.executeUpdate();
                addressItem.setId(lastInsertId(ps));
                ps.close();
            } else {
                query.append("UPDATE ");
                query.append(tableName);
                query.append(" SET title=?, last_update = NOW() WHERE id=?");
                ps = con.prepareStatement(query.toString());
                ps.setString(1, addressItem.getTitle());
                ps.setInt(2, addressItem.getId());
                ps.executeUpdate();
                ps.close();
            }
        }
        return addressItem;
    }

    private void deleteAddressItem(String tableName, String columnName, int id) throws SQLException, BGMessageException {
        StringBuilder query = new StringBuilder();

        query.append(SQL_SELECT);
        query.append("COUNT(*)");
        query.append(SQL_FROM);
        query.append(Tables.TABLE_ADDRESS_HOUSE);
        query.append(SQL_WHERE);
        query.append(columnName + "=?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
            throw new BGMessageException("Сущность привязана к дому.");
        }
        ps.close();

        query.setLength(0);
        query.append(SQL_DELETE_FROM);
        query.append(tableName);
        query.append(SQL_WHERE);
        query.append("id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public AddressHouse updateAddressHouse(AddressHouse addressHouse) throws SQLException {
        if (addressHouse != null) {
            int index = 1;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();

            if (addressHouse.getId() <= 0) {
                query.append("INSERT INTO ");
                query.append(Tables.TABLE_ADDRESS_HOUSE);
                query.append(" SET area_id=?, quarter_id=?, street_id=?, house=?, frac=?, post_index=?, comment=?, last_update=NOW()");
                ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(index++, addressHouse.getAreaId());
                ps.setInt(index++, addressHouse.getQuarterId());
                ps.setInt(index++, addressHouse.getStreetId());
                ps.setInt(index++, addressHouse.getHouse());
                ps.setString(index++, addressHouse.getFrac() == null ? "" : addressHouse.getFrac());
                ps.setString(index++, addressHouse.getPostIndex());
                ps.setString(index++, addressHouse.getComment());
                ps.executeUpdate();
                addressHouse.setId(lastInsertId(ps));
                ps.close();
            } else {
                query.append("UPDATE ");
                query.append(Tables.TABLE_ADDRESS_HOUSE);
                query.append(" SET area_id=?, quarter_id=?, street_id=?, house=?, frac=?, post_index=?, comment=?, last_update=NOW() WHERE id=?");
                ps = con.prepareStatement(query.toString());
                ps.setInt(index++, addressHouse.getAreaId());
                ps.setInt(index++, addressHouse.getQuarterId());
                ps.setInt(index++, addressHouse.getStreetId());
                ps.setInt(index++, addressHouse.getHouse());
                ps.setString(index++, addressHouse.getFrac() == null ? "" : addressHouse.getFrac());
                ps.setString(index++, addressHouse.getPostIndex());
                ps.setString(index++, addressHouse.getComment());
                ps.setInt(index++, addressHouse.getId());
                ps.executeUpdate();
                ps.close();
            }
        }
        return addressHouse;
    }

    public void deleteAddressHouse(int id) throws SQLException, BGMessageException {
        StringBuilder query = new StringBuilder();

        query.append("SELECT COUNT(*) FROM");
        query.append(Tables.TABLE_PARAM_ADDRESS);
        query.append("WHERE house_id=?");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
            throw new BGMessageException("Дом используется в параметрах.");
        }
        ps.close();

        query.setLength(0);
        query.append(SQL_DELETE_FROM);
        query.append(Tables.TABLE_ADDRESS_HOUSE);
        query.append("WHERE id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public static AddressHouse getAddressHouseFromRs(ResultSet rs, String prefix, int loadLevel) throws SQLException {
        AddressHouse addressHouse = new AddressHouse();

        addressHouse.setId(rs.getInt(prefix + "id"));
        addressHouse.setAreaId(rs.getInt(prefix + "area_id"));
        addressHouse.setQuarterId(rs.getInt(prefix + "quarter_id"));
        addressHouse.setStreetId(rs.getInt(prefix + "street_id"));
        addressHouse.setHouse(rs.getInt(prefix + "house"));
        addressHouse.setFrac(rs.getString(prefix + "frac"));
        addressHouse.setPostIndex(rs.getString(prefix + "post_index"));
        addressHouse.setComment(rs.getString(prefix + "comment"));

        if (loadLevel >= LOAD_LEVEL_STREET) {
            AddressItem addressStreet = getAddressItemFromRs(rs, "street.");
            addressHouse.setAddressStreet(addressStreet);

            addressHouse.setAddressArea(getAddressItemFromRs(rs, "area."));
            addressHouse.setAddressQuarter(getAddressItemFromRs(rs, "quarter."));

            if (loadLevel >= LOAD_LEVEL_CITY) {
                AddressCity addressCity = getAddressCityFromRs(rs, "city.");
                addressStreet.setAddressCity(addressCity);
                if (loadLevel >= LOAD_LEVEL_COUNTRY) {
                    AddressCountry addressCountry = getAddressCountryFromRs(rs, "country.");
                    addressCity.setAddressCountry(addressCountry);
                }
            }
        }

        return addressHouse;
    }

    public static AddressItem getAddressItemFromRs(ResultSet rs, String prefix) throws SQLException {
        AddressItem addressItem = new AddressItem();

        addressItem.setId(rs.getInt(prefix + "id"));
        addressItem.setCityId(rs.getInt(prefix + "city_id"));
        addressItem.setTitle(rs.getString(prefix + "title"));

        return addressItem;
    }

    public static AddressCountry getAddressCountryFromRs(ResultSet rs, String prefix) throws SQLException {
        AddressCountry addressCountry = new AddressCountry();

        addressCountry.setId(rs.getInt(prefix + "id"));
        addressCountry.setTitle(rs.getString(prefix + "title"));

        return addressCountry;
    }

    public static AddressCity getAddressCityFromRs(ResultSet rs, String prefix) throws SQLException {
        AddressCity addressCity = new AddressCity();

        addressCity.setId(rs.getInt(prefix + "id"));
        addressCity.setCountryId(rs.getInt(prefix + "country_id"));
        addressCity.setTitle(rs.getString(prefix + "title"));

        return addressCity;
    }

    public List<AddressCountry> getUpdatedCountries(long time, int[] countriesId) throws SQLException {
        List<AddressCountry> result = new ArrayList<>();

        String ids = getIdsString(countriesId);
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(Tables.TABLE_ADDRESS_COUNTRY);
        query.append(" WHERE last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        try (var ps = con.prepareStatement(query.toString())) {
            ps.setTimestamp(1, new Timestamp(time));

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(getAddressCountryFromRs(rs, ""));
        }

        return result;
    }

    public List<AddressCity> getUpdatedCities(long time, int[] citiesId) throws SQLException {
        List<AddressCity> result = new ArrayList<>();

        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(Tables.TABLE_ADDRESS_CITY);
        query.append(" WHERE last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        try (var ps = con.prepareStatement(query.toString())) {
            ps.setTimestamp(1, new Timestamp(time));

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(getAddressCityFromRs(rs, ""));
        }

        return result;
    }

    public List<AddressHouse> getUpdatedHouses(long time, int[] citiesId) throws SQLException {
        List<AddressHouse> result = new ArrayList<>();

        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT h.* FROM ");
        query.append(Tables.TABLE_ADDRESS_HOUSE);
        query.append(" AS h LEFT JOIN ");
        query.append(Tables.TABLE_ADDRESS_STREET);
        query.append(" AS s ON h.street_id=s.id WHERE h.last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND s.city_id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            ps.setTimestamp(1, new Timestamp(time));

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(getAddressHouseFromRs(rs, "h.", 0));
        }

        return result;
    }

    private List<AddressItem> getUpdatedItems(String tableName, long time, int[] citiesId) throws SQLException {
        List<AddressItem> result = new ArrayList<>();

        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(tableName);
        query.append(" WHERE last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND city_id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        try (var ps = con.prepareStatement(query.toString())) {
            ps.setTimestamp(1, new Timestamp(time));

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(getAddressItemFromRs(rs, ""));
        }

        return result;
    }

    public List<AddressItem> getUpdatedAreas(long time, int[] citiesId) throws SQLException {
        return getUpdatedItems(Tables.TABLE_ADDRESS_AREA, time, citiesId);
    }

    public List<AddressItem> getUpdatedQuarters(long time, int[] citiesId) throws SQLException {
        return getUpdatedItems(Tables.TABLE_ADDRESS_QUARTER, time, citiesId);
    }

    public List<AddressItem> getUpdatedStreets(long time, int[] citiesId) throws SQLException {
        return getUpdatedItems(Tables.TABLE_ADDRESS_STREET, time, citiesId);
    }

    public List<Integer> getCountryIdByCityId(int[] citiesId) throws SQLException {
        List<Integer> list = new ArrayList<>();

        Set<Integer> countryIdSet = new HashSet<>();
        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT country_id FROM ");
        query.append(Tables.TABLE_ADDRESS_CITY);
        query.append(" WHERE true");
        if (ids.length() > 0) {
            query.append(" AND id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Integer countryId = rs.getInt(1);
            if (!countryIdSet.contains(countryId)) {
                countryIdSet.add(countryId);
                list.add(countryId);
            }
        }
        ps.close();

        return list;
    }

    /**
     * Поиск улиц по подстроке в заданных городах
     * @param streetSubstring подстрока поиска
     * @param cityIds список id городов, в которых нужно искать
     * если cityIds == null, то поиск по всем городам (город не задан)
     * @return
     */
    public List<AddressItem> getAddressStreetsBySubstring(String streetSubstring, List<Integer> cityIds) throws SQLException {
        List<AddressItem> matchStreets = new ArrayList<>();
        if (cityIds == null || cityIds.size() > 0) {
            StringBuilder query = new StringBuilder("SELECT * FROM ");
            query.append(Tables.TABLE_ADDRESS_STREET);
            query.append(" WHERE title like ?");
            if (cityIds != null) {
                query.append(" AND ");
                for (int i = 0; i < cityIds.size(); i++) {
                    if (i == 0) {
                        query.append(" ( ");
                    }
                    query.append(" city_id=? ");
                    if (i != cityIds.size() - 1) {
                        query.append(" OR ");
                    } else {
                        query.append(" ) ");
                    }
                }
            }

            PreparedStatement ps = con.prepareStatement(query.toString());
            int index = 1;
            ps.setString(index++, "%" + streetSubstring + "%");
            if (cityIds != null) {
                for (Integer cityId : cityIds) {
                    ps.setInt(index++, cityId);
                }
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                matchStreets.add(getAddressItemFromRs(rs, ""));
            }
            ps.close();
        }

        return matchStreets;
    }

    //TODO: Успешность применения функции зависит от формата адреса. Найти использование и переписать.
    public int getCityIdByAddress(String address) throws SQLException {
        int cityId = -1;

        PreparedStatement ps = con.prepareStatement("SELECT id FROM " + Tables.TABLE_ADDRESS_CITY + "WHERE title = ?");
        ps.setString(1, address.split(",")[0]);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            cityId = rs.getInt(1);
        }

        ps.close();

        return cityId;
    }

    public List<Integer> getHouseIdsByStreetAndHouse(int streetId, String house, List<Integer> cityIds) throws SQLException {
        List<Integer> houseIds = new ArrayList<>();
        if (cityIds == null || cityIds.size() > 0) {
            AddressHouse houseAndFrac = new AddressHouse().withHouseAndFrac(house);

            PreparedQuery pq = new PreparedQuery(con);

            pq.addQuery("SELECT house.id FROM ");
            pq.addQuery(Tables.TABLE_ADDRESS_HOUSE);
            pq.addQuery(" AS house");
            if (cityIds != null) {
                pq.addQuery(" LEFT JOIN ");
                pq.addQuery(Tables.TABLE_ADDRESS_STREET);
                pq.addQuery(" AS street ON house.street_id=street.id");
            }
            pq.addQuery(" WHERE street_id=?");
            pq.addInt(streetId);

            if (houseAndFrac.getHouse() > 0) {
                pq.addQuery(" AND house=?");
                pq.addInt(houseAndFrac.getHouse());
            }

            String houseFrac = houseAndFrac.getFrac();

            if (houseFrac != null && !houseFrac.equals("*")) {
                pq.addString(houseFrac);

                if (houseFrac.startsWith("/")) {
                    pq.addQuery(" AND (frac=? OR frac=?)");
                    pq.addString(houseFrac);
                } else {
                    pq.addQuery(" AND frac=?");
                }
            }

            if (cityIds != null) {
                pq.addQuery(" AND street.city_id IN (");
                pq.addQuery(Utils.toString(cityIds));
                pq.addQuery(")");
            }

            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                houseIds.add(rs.getInt(1));
            }
            pq.close();
        }
        return houseIds;
    }

    private String getIdsString(int[] array) {
        StringBuilder ids = new StringBuilder();
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(array[i]);
            }
        }
        return ids.toString();
    }
}
