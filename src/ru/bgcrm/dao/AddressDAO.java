package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_AREA;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_CITY;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_CONFIG;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_COUNTRY;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_QUARTER;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;

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

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.ConfigRecord;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.address.AddressCity;
import ru.bgcrm.model.param.address.AddressCountry;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.param.address.AddressItem;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class AddressDAO extends CommonDAO {
    // Уровни загрузки адресных данных.
    public static final int LOAD_LEVEL_HOUSE = 1;
    // По этому уровню загружаются улицы, районы и дома.
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

    public void searchAddressCountryList(SearchResult<AddressCountry> searchResult, String title) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<AddressCountry> result = searchResult.getList();

            PreparedDelay ps = new PreparedDelay(con);
            ps.addQuery("SELECT SQL_CALC_FOUND_ROWS * ");
            ps.addQuery("FROM ");
            ps.addQuery(TABLE_ADDRESS_COUNTRY);

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
                page.setRecordCount(getFoundRows(ps.getPrepared()));
            }

            ps.close();
        }
    }

    public AddressCountry getAddressCountry(int id) throws SQLException {
        AddressCountry addressCountry = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(TABLE_ADDRESS_COUNTRY);
        query.append(" WHERE id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            addressCountry = getAddressCountryFromRs(rs, "");
        }

        ps.close();
        if (addressCountry != null) {
            ConfigDAO configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
            addressCountry.setConfig(configDAO.getConfigRecordMap(TABLE_ADDRESS_COUNTRY, addressCountry.getId()));
        }

        return addressCountry;
    }

    public void searchAddressCityList(SearchResult<AddressCity> searchResult, int countryId, String title, boolean loadCountryData,
            Set<Integer> cityIdFilter) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<AddressCity> result = searchResult.getList();

            PreparedDelay ps = new PreparedDelay(con);
            ps.addQuery("SELECT SQL_CALC_FOUND_ROWS * ");
            ps.addQuery("FROM ");
            ps.addQuery(TABLE_ADDRESS_CITY);
            ps.addQuery("AS city ");

            if (loadCountryData) {
                ps.addQuery("LEFT JOIN ");
                ps.addQuery(TABLE_ADDRESS_COUNTRY);
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

            setRecordCount(page, ps.getPrepared());

            ps.close();
        }
    }

    public AddressCity getAddressCity(int id, boolean loadCountryData) throws SQLException {
        AddressCity addressCity = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(TABLE_ADDRESS_CITY);
        query.append(" AS city");
        if (loadCountryData) {
            query.append(" LEFT JOIN ");
            query.append(TABLE_ADDRESS_COUNTRY);
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
        if (addressCity != null) {
            ConfigDAO configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
            addressCity.setConfig(configDAO.getConfigRecordMap(TABLE_ADDRESS_CITY, addressCity.getId()));
        }

        return addressCity;
    }

    public void searchAddressAreaList(SearchResult<AddressItem> searchResult, int cityId) throws SQLException {
        searchAddressItemList(TABLE_ADDRESS_AREA, searchResult, Collections.singleton(cityId), null, false, false);
    }

    public void searchAddressAreaList(SearchResult<AddressItem> searchResult, int cityId, String title, boolean loadCountryData, boolean loadCityData)
            throws SQLException {
        searchAddressItemList(TABLE_ADDRESS_AREA, searchResult, Collections.singleton(cityId), title, loadCountryData, loadCityData);
    }

    public AddressItem getAddressArea(int id, boolean loadCountryData, boolean loadCityData) throws SQLException {
        return getAddressItem(TABLE_ADDRESS_AREA, id, loadCountryData, loadCityData);
    }

    public void searchAddressQuarterList(SearchResult<AddressItem> searchResult, int cityId) throws SQLException {
        searchAddressItemList(TABLE_ADDRESS_QUARTER, searchResult, Collections.singleton(cityId), null, false, false);
    }

    public void searchAddressQuarterList(SearchResult<AddressItem> searchResult, int cityId, String title, boolean loadCountryData,
            boolean loadCityData) throws SQLException {
        searchAddressItemList(TABLE_ADDRESS_QUARTER, searchResult, Collections.singleton(cityId), title, loadCountryData, loadCityData);
    }

    public AddressItem getAddressQuarter(int id, boolean loadCountryData, boolean loadCityData) throws SQLException {
        return getAddressItem(TABLE_ADDRESS_QUARTER, id, loadCountryData, loadCityData);
    }

    public void searchAddressStreetList(SearchResult<AddressItem> searchResult, int cityId) throws SQLException {
        searchAddressItemList(TABLE_ADDRESS_STREET, searchResult, Collections.singleton(cityId), null, false, false);
    }

    public void searchAddressStreetList(SearchResult<AddressItem> searchResult, Set<Integer> cityIds, String title, boolean loadCountryData,
            boolean loadCityData) throws SQLException {
        searchAddressItemList(TABLE_ADDRESS_STREET, searchResult, cityIds, title, loadCountryData, loadCityData);
    }

    public AddressItem getAddressStreet(int id, boolean loadCountryData, boolean loadCityData) throws SQLException {
        return getAddressItem(TABLE_ADDRESS_STREET, id, loadCountryData, loadCityData);
    }

    private void searchAddressItemList(String tableName, SearchResult<AddressItem> searchResult, Set<Integer> cityIds, String title,
            boolean loadCountryData, boolean loadCityData) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<AddressItem> result = searchResult.getList();

            PreparedDelay ps = new PreparedDelay(con);
            ps.addQuery("SELECT SQL_CALC_FOUND_ROWS * ");
            ps.addQuery("FROM ");
            ps.addQuery(tableName);
            ps.addQuery(" AS item ");

            if (loadCityData) {
                ps.addQuery("LEFT JOIN ");
                ps.addQuery(TABLE_ADDRESS_CITY);
                ps.addQuery(" AS city ON item.city_id = city.id ");

                if (loadCountryData) {
                    ps.addQuery("LEFT JOIN ");
                    ps.addQuery(TABLE_ADDRESS_COUNTRY);
                    ps.addQuery(" AS country ON city.country_id = country.id ");
                }
            }

            ps.addQuery("WHERE 1=1 ");

            if (CollectionUtils.isNotEmpty(cityIds)) {
                boolean hasPositiveCityIds = false;

                Iterator<Integer> iterator = cityIds.iterator();
                while (!hasPositiveCityIds && iterator.hasNext()) {
                    Integer cityId = iterator.next();
                    if (cityId > 0) {
                        hasPositiveCityIds = true;
                        ps.addQuery(" AND city_id IN( " + Utils.toString(cityIds) + " ) ");
                    }
                }
            }

            if (!Utils.isEmptyString(title)) {
                ps.addQuery(" AND item.title LIKE ? ");
                ps.addString(title);
            }

            ps.addQuery(" ORDER BY item.title");
            ps.addQuery(getPageLimit(page));

            ResultSet rs = ps.executeQuery();

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
                page.setRecordCount(getFoundRows(ps.getPrepared()));
            }

            ps.close();
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
            query.append(TABLE_ADDRESS_CITY);
            query.append(" AS city ON item.city_id=city.id");
            if (loadCountryData) {
                query.append(" LEFT JOIN ");
                query.append(TABLE_ADDRESS_COUNTRY);
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
        if (addressItem != null) {
            ConfigDAO configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
            addressItem.setConfig(configDAO.getConfigRecordMap(tableName, addressItem.getId()));
        }

        return addressItem;
    }

    public void searchAddressHouseList(SearchResult<AddressHouse> searchResult, int streetId, String housePrefix) throws SQLException {
        searchAddressHouseList(searchResult, streetId, housePrefix, false, false, false, false);
    }

    public void searchAddressHouseList(SearchResult<AddressHouse> searchResult, int streetId, String house, boolean absolute, boolean loadCountryData,
            boolean loadCityData, boolean loadStreetData) throws SQLException {
        if (searchResult != null && streetId > 0) {
            Page page = searchResult.getPage();
            List<AddressHouse> result = searchResult.getList();

            PreparedDelay pd = new PreparedDelay(con);

            AddressHouse searchParams = AddressHouse.extractHouseAndFrac(house);

            int number = searchParams.getHouse();
            String frac = searchParams.getFrac();

            int loadLevel = getHouseLoadLevel(loadCountryData, loadCityData, loadStreetData);

            pd.addQuery("SELECT SQL_CALC_FOUND_ROWS * FROM ");
            pd.addQuery(TABLE_ADDRESS_HOUSE);
            pd.addQuery(" AS house");
            addHouseSelectQueryJoins(pd.getQuery(), loadLevel);

            pd.addQuery(" WHERE 1=1 AND house.street_id=?");
            pd.addInt(streetId);

            if (absolute) {
                if (number > 0) {
                    pd.addQuery(" AND house.house=? ");
                    pd.addInt(number);

                    if (frac != null) {
                        pd.addQuery(" AND house.frac=?");
                        pd.addString(frac);
                    }
                }
            } else {
                if (number > 0) {
                    pd.addQuery(" AND house.house LIKE CONCAT(?, '%')");
                    pd.addInt(number);
                }
                if (Utils.notBlankString(frac)) {
                    pd.addQuery(" AND house.frac LIKE CONCAT('%', ?, '%')");
                    pd.addString(frac);
                }
            }

            pd.addQuery(" ORDER BY house.house, house.frac");
            pd.addQuery(getPageLimit(page));

            ResultSet rs = pd.executeQuery();
            while (rs.next()) {
                result.add(getAddressHouseFromRs(rs, "house.", loadLevel));
            }
            if (page != null) {
                page.setRecordCount(getFoundRows(pd.getPrepared()));
            }
            pd.close();
        }
    }

    public AddressHouse getAddressHouse(int id, boolean loadCountryData, boolean loadCityData, boolean loadStreetData) throws SQLException {
        AddressHouse addressHouse = null;

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();

        int loadLevel = getHouseLoadLevel(loadCountryData, loadCityData, loadStreetData);

        query.append("SELECT * FROM ");
        query.append(TABLE_ADDRESS_HOUSE);
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
        if (addressHouse != null) {
            ConfigDAO configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
            addressHouse.setConfig(configDAO.getConfigRecordMap(TABLE_ADDRESS_HOUSE, addressHouse.getId()));
        }

        return addressHouse;
    }

    public static void addHouseSelectQueryJoins(StringBuilder query, int loadLevel) {
        if (loadLevel >= LOAD_LEVEL_STREET) {
            query.append(" LEFT JOIN ");
            query.append(TABLE_ADDRESS_STREET);
            query.append(" AS street ON house.street_id=street.id");
            query.append(" LEFT JOIN ");
            query.append(TABLE_ADDRESS_AREA);
            query.append(" AS area ON house.area_id=area.id");
            query.append(" LEFT JOIN ");
            query.append(TABLE_ADDRESS_QUARTER);
            query.append(" AS quarter ON house.quarter_id=quarter.id");

            if (loadLevel >= LOAD_LEVEL_CITY) {
                query.append(" LEFT JOIN ");
                query.append(TABLE_ADDRESS_CITY);
                query.append(" AS city ON street.city_id=city.id");

                if (loadLevel >= LOAD_LEVEL_COUNTRY) {
                    query.append(" LEFT JOIN ");
                    query.append(TABLE_ADDRESS_COUNTRY);
                    query.append(" AS country ON city.country_id=country.id");
                }
            }
        }
    }

    public void updateAddressCountry(AddressCountry addressCountry) throws BGException {
        try {
            if (addressCountry != null) {
                PreparedStatement ps = null;
                StringBuilder query = new StringBuilder();

                if (addressCountry.getId() <= 0) {
                    query.append("INSERT INTO ");
                    query.append(TABLE_ADDRESS_COUNTRY);
                    query.append(" SET title=?, last_update = NOW()");
                    ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, addressCountry.getTitle());
                    ps.executeUpdate();
                    addressCountry.setId(lastInsertId(ps));
                    ps.close();
                } else {
                    query.append("UPDATE ");
                    query.append(TABLE_ADDRESS_COUNTRY);
                    query.append(" SET title=?, last_update = NOW() WHERE id=?");
                    ps = con.prepareStatement(query.toString());
                    ps.setString(1, addressCountry.getTitle());
                    ps.setInt(2, addressCountry.getId());
                    ps.executeUpdate();
                    ps.close();
                }

                new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(TABLE_ADDRESS_COUNTRY, addressCountry.getId(),
                        addressCountry.getConfig());
            }
        } catch (SQLException e) {
            sqlToBgException(e);
        }
    }

    public void deleteAddressCountry(int id) throws BGException {
        try {
            StringBuilder query = new StringBuilder();

            query.append("SELECT COUNT(*) FROM ");
            query.append(TABLE_ADDRESS_CITY);
            query.append("WHERE country_id=?");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new BGMessageException("Страна используется в городах.");
            }
            ps.close();

            query.setLength(0);
            query.append(SQL_DELETE);
            query.append(TABLE_ADDRESS_COUNTRY);
            query.append("WHERE id=?");

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(TABLE_ADDRESS_COUNTRY, id, new ArrayList<ConfigRecord>());
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void updateAddressCity(AddressCity addressCity) throws BGException {
        try {
            if (addressCity != null) {
                PreparedStatement ps = null;
                StringBuilder query = new StringBuilder();

                if (addressCity.getId() <= 0) {
                    query.append("INSERT INTO ");
                    query.append(TABLE_ADDRESS_CITY);
                    query.append(" SET country_id=?, title=?, last_update = NOW()");
                    ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, addressCity.getCountryId());
                    ps.setString(2, addressCity.getTitle());
                    ps.executeUpdate();
                    addressCity.setId(lastInsertId(ps));
                    ps.close();
                } else {
                    query.append("UPDATE ");
                    query.append(TABLE_ADDRESS_CITY);
                    query.append(" SET title=?, last_update = NOW() WHERE id=?");
                    ps = con.prepareStatement(query.toString());
                    ps.setString(1, addressCity.getTitle());
                    ps.setInt(2, addressCity.getId());
                    ps.executeUpdate();
                    ps.close();
                }
                new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(TABLE_ADDRESS_CITY, addressCity.getId(), addressCity.getConfig());
            }
        } catch (SQLException e) {
            sqlToBgException(e);
        }
    }

    public void deleteAddressCity(int id) throws BGException {
        try {
            checkItem(id, TABLE_ADDRESS_STREET, "улицах");
            checkItem(id, TABLE_ADDRESS_QUARTER, "кварталах");
            checkItem(id, TABLE_ADDRESS_AREA, "районах");

            StringBuilder query = new StringBuilder();
            query.append(SQL_DELETE);
            query.append(TABLE_ADDRESS_CITY);
            query.append("WHERE id=?");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(TABLE_ADDRESS_CITY, id, new ArrayList<ConfigRecord>());
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    //TODO: Убедиться в необходимости возврата PreparedStatement
    public PreparedStatement checkItem(int id, String table, String title) throws SQLException, BGMessageException {
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
        return ps;
    }

    public void updateAddressArea(AddressItem addressItem) throws BGException {
        updateAddressItem(TABLE_ADDRESS_AREA, addressItem);
    }

    public void updateAddressQuarter(AddressItem addressItem) throws BGException {
        updateAddressItem(TABLE_ADDRESS_QUARTER, addressItem);
    }

    public void updateAddressStreet(AddressItem addressItem) throws BGException {
        updateAddressItem(TABLE_ADDRESS_STREET, addressItem);
    }

    public void deleteAddressArea(int id) throws BGException {
        deleteAddressItem(TABLE_ADDRESS_AREA, "area_id", id);
    }

    public void deleteAddressQuarter(int id) throws BGException {
        deleteAddressItem(TABLE_ADDRESS_QUARTER, "quarter_id", id);
    }

    public void deleteAddressStreet(int id) throws BGException {
        deleteAddressItem(TABLE_ADDRESS_STREET, "street_id", id);
    }

    private void updateAddressItem(String tableName, AddressItem addressItem) throws BGException {
        try {
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
                new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(tableName, addressItem.getId(), addressItem.getConfig());
            }
        } catch (SQLException e) {
            sqlToBgException(e);
        }
    }

    private void deleteAddressItem(String tableName, String columnName, int id) throws BGException {
        try {
            StringBuilder query = new StringBuilder();

            query.append(SQL_SELECT);
            query.append("COUNT(*)");
            query.append(SQL_FROM);
            query.append(TABLE_ADDRESS_HOUSE);
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
            query.append(SQL_DELETE);
            query.append(tableName);
            query.append(SQL_WHERE);
            query.append("id=?");

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(tableName, id, new ArrayList<ConfigRecord>());
        } catch (SQLException e) {
            sqlToBgException(e);
        }
    }

    public void updateAddressHouse(AddressHouse addressHouse) throws SQLException {
        if (addressHouse != null) {
            int index = 1;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();

            if (addressHouse.getId() <= 0) {
                query.append("INSERT INTO ");
                query.append(TABLE_ADDRESS_HOUSE);
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
                query.append(TABLE_ADDRESS_HOUSE);
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
            new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(TABLE_ADDRESS_HOUSE, addressHouse.getId(), addressHouse.getConfig());
        }
    }

    public void deleteAddressHouse(int id) throws BGException {
        try {
            StringBuilder query = new StringBuilder();

            query.append("SELECT COUNT(*) FROM");
            query.append(TABLE_PARAM_ADDRESS);
            query.append("WHERE house_id=?");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new BGMessageException("Дом используется в параметрах.");
            }
            ps.close();

            query.setLength(0);
            query.append(SQL_DELETE);
            query.append(TABLE_ADDRESS_HOUSE);
            query.append("WHERE id=?");

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            new ConfigDAO(con, TABLE_ADDRESS_CONFIG).updateConfigForRecord(TABLE_ADDRESS_HOUSE, id, new ArrayList<ConfigRecord>());
        } catch (SQLException e) {
            throw new BGException(e);
        }
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
        List<AddressCountry> result = new ArrayList<AddressCountry>();

        String ids = getIdsString(countriesId);
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(TABLE_ADDRESS_COUNTRY);
        query.append(" WHERE last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setTimestamp(1, new Timestamp(time));

        ResultSet rs = ps.executeQuery();
        AddressCountry addressCountry;
        ConfigDAO configDAO;
        while (rs.next()) {
            addressCountry = getAddressCountryFromRs(rs, "");
            if (result != null) {
                configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
                addressCountry.setConfig(configDAO.getConfigRecordMap(TABLE_ADDRESS_COUNTRY, addressCountry.getId()));
            }
            result.add(addressCountry);
        }

        ps.close();

        return result;
    }

    public List<AddressCity> getUpdatedCities(long time, int[] citiesId) throws SQLException {
        List<AddressCity> result = new ArrayList<AddressCity>();

        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(TABLE_ADDRESS_CITY);
        query.append(" WHERE last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setTimestamp(1, new Timestamp(time));

        ResultSet rs = ps.executeQuery();
        AddressCity addressCity;
        ConfigDAO configDAO;
        while (rs.next()) {
            addressCity = getAddressCityFromRs(rs, "");
            configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
            addressCity.setConfig(configDAO.getConfigRecordMap(TABLE_ADDRESS_CITY, addressCity.getId()));
            result.add(addressCity);
        }
        ps.close();

        return result;
    }

    public List<AddressHouse> getUpdatedHouses(long time, int[] citiesId) throws SQLException {
        List<AddressHouse> result = new ArrayList<AddressHouse>();

        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT h.* FROM ");
        query.append(TABLE_ADDRESS_HOUSE);
        query.append(" AS h LEFT JOIN ");
        query.append(TABLE_ADDRESS_STREET);
        query.append(" AS s ON h.street_id=s.id WHERE h.last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND s.city_id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setTimestamp(1, new Timestamp(time));

        ResultSet rs = ps.executeQuery();
        AddressHouse addressHouse;
        ConfigDAO configDAO;
        while (rs.next()) {
            addressHouse = getAddressHouseFromRs(rs, "h.", 0);
            configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
            addressHouse.setConfig(configDAO.getConfigRecordMap(TABLE_ADDRESS_HOUSE, addressHouse.getId()));
            result.add(addressHouse);
        }
        ps.close();

        return result;
    }

    private List<AddressItem> getUpdatedItems(String tableName, long time, int[] citiesId) throws SQLException {
        List<AddressItem> result = new ArrayList<AddressItem>();

        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(tableName);
        query.append(" WHERE last_update>=?");
        if (ids.length() > 0) {
            query.append(" AND city_id IN ( ");
            query.append(ids);
            query.append(" )");
        }

        PreparedStatement ps = con.prepareStatement(query.toString());
        ps.setTimestamp(1, new Timestamp(time));

        ResultSet rs = ps.executeQuery();
        AddressItem addressItem;
        ConfigDAO configDAO;
        while (rs.next()) {
            addressItem = getAddressItemFromRs(rs, "");
            configDAO = new ConfigDAO(con, TABLE_ADDRESS_CONFIG);
            addressItem.setConfig(configDAO.getConfigRecordMap(tableName, addressItem.getId()));
            result.add(addressItem);
        }
        ps.close();

        return result;
    }

    public List<AddressItem> getUpdatedAreas(long time, int[] citiesId) throws SQLException {
        return getUpdatedItems(TABLE_ADDRESS_AREA, time, citiesId);
    }

    public List<AddressItem> getUpdatedQuarters(long time, int[] citiesId) throws SQLException {
        return getUpdatedItems(TABLE_ADDRESS_QUARTER, time, citiesId);
    }

    public List<AddressItem> getUpdatedStreets(long time, int[] citiesId) throws SQLException {
        return getUpdatedItems(TABLE_ADDRESS_STREET, time, citiesId);
    }

    public List<Integer> getCountryIdByCityId(int[] citiesId) throws SQLException {
        List<Integer> list = new ArrayList<Integer>();

        Set<Integer> countryIdSet = new HashSet<Integer>();
        String ids = getIdsString(citiesId);
        StringBuilder query = new StringBuilder("SELECT country_id FROM ");
        query.append(TABLE_ADDRESS_CITY);
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
        List<AddressItem> matchStreets = new ArrayList<AddressItem>();
        if (cityIds == null || cityIds.size() > 0) {
            StringBuilder query = new StringBuilder("SELECT * FROM ");
            query.append(TABLE_ADDRESS_STREET);
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

        PreparedStatement ps = con.prepareStatement("SELECT id FROM " + TABLE_ADDRESS_CITY + "WHERE title = ?");
        ps.setString(1, address.split(",")[0]);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            cityId = rs.getInt(1);
        }

        ps.close();

        return cityId;
    }

    public List<Integer> getHouseIdsByStreetAndHouse(int streetId, String house, List<Integer> cityIds) throws SQLException {
        List<Integer> houseIds = new ArrayList<Integer>();
        if (cityIds == null || cityIds.size() > 0) {
            AddressHouse houseAndFrac = AddressHouse.extractHouseAndFrac(house);

            PreparedDelay pd = new PreparedDelay(con);

            pd.addQuery("SELECT house.id FROM ");
            pd.addQuery(TABLE_ADDRESS_HOUSE);
            pd.addQuery(" AS house");
            if (cityIds != null) {
                pd.addQuery(" LEFT JOIN ");
                pd.addQuery(TABLE_ADDRESS_STREET);
                pd.addQuery(" AS street ON house.street_id=street.id");
            }
            pd.addQuery(" WHERE street_id=?");
            pd.addInt(streetId);

            if (houseAndFrac.getHouse() > 0) {
                pd.addQuery(" AND house=?");
                pd.addInt(houseAndFrac.getHouse());
            }

            String houseFrac = houseAndFrac.getFrac();

            if (houseFrac != null && !houseFrac.equals("*")) {
                pd.addString(houseFrac);

                if (houseFrac.startsWith("/")) {
                    pd.addQuery(" AND (frac=? OR frac=?)");
                    pd.addString(houseFrac);
                } else {
                    pd.addQuery(" AND frac=?");
                }
            }

            if (cityIds != null) {
                pd.addQuery(" AND street.city_id IN (");
                pd.addQuery(Utils.toString(cityIds));
                pd.addQuery(")");
            }

            ResultSet rs = pd.executeQuery();
            while (rs.next()) {
                houseIds.add(rs.getInt(1));
            }
            pd.close();
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
