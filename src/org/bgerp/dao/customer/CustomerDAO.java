package org.bgerp.dao.customer;

import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER_GROUP;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER_LINK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.bgerp.action.base.form.Response;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.dao.expression.ParamExpressionObject;
import org.bgerp.dao.param.OldParamSearchDAO;
import org.bgerp.dao.param.Tables;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;
import org.bgerp.util.text.PatternFormatter;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ParamValueSelect;
import ru.bgcrm.dao.PatternDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.client.CustomerTitleChangedEvent;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.param.Pattern;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.util.Utils;

public class CustomerDAO extends CommonDAO {
    private boolean history;
    private int userId;

    public CustomerDAO(Connection con) {
        super(con);
    }

    public CustomerDAO(Connection con, boolean history, int userId) {
        super(con);
        this.history = history;
        this.userId = userId;
    }

    /**
     * Selects customers by title
     * @param searchResult
     * @param title
     */
    public void searchCustomerList(Pageable<Customer> searchResult, String title) {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Customer> list = searchResult.getList();

            StringBuilder selectPart = new StringBuilder();
            StringBuilder joinPart = new StringBuilder();

            String referenceTemplate = addCustomerReferenceQuery(selectPart, joinPart);

            PreparedQuery ps = new PreparedQuery(con);

            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT_COUNT_ROWS);
            query.append(selectPart);
            query.append("customer.* FROM " + TABLE_CUSTOMER + " AS customer");
            query.append(joinPart);
            if (Utils.notBlankString(title)) {
                query.append(" WHERE title LIKE ? ");
                ps.addString(title);
            }
            query.append(" ORDER BY title");
            query.append(page.getLimitSql());

            ps.addQuery(query.toString());

            extractCustomersWithRef(page, list, referenceTemplate, ps);
        }
    }

    private void extractCustomersWithRef(Page page, List<Customer> list, String referenceTemplate, PreparedQuery ps) {
        try {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer customer = getCustomerFromRs(rs, "");
                list.add(customer);

                if (Utils.notBlankString(referenceTemplate)) {
                    String reference = PatternFormatter.processPattern(referenceTemplate, variable -> {
                        String value = "";
                        try {
                            if (variable.startsWith("param:"))
                                value = rs.getString(variable.replace(':', '_') + "_val");
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        return value;
                    });
                    customer.setReference(reference);
                }
            }
            if (page != null) {
                page.setRecordCount(ps.getPrepared());
            }
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }

    private String addCustomerReferenceQuery(final StringBuilder selectPart, final StringBuilder joinPart) {
        String referenceTemplate = Setup.getSetup().get("customer.reference.pattern", "");
        if (Utils.notBlankString(referenceTemplate)) {
            PatternFormatter.processPattern(referenceTemplate, variable -> {
                if (variable.startsWith("param:")) {
                    ParamValueSelect.paramSelectQuery(variable, "customer.id", selectPart, joinPart, true);
                    selectPart.append(", ");
                }
                return "";
            });
        }
        return referenceTemplate;
    }

    /**
     * Selects customers by an Email-type parameter
     * @param searchResult
     * @param emailParamIdList
     * @param email Email, search is done by exact match and domain match
     */
    public void searchCustomerListByEmail(Pageable<ParameterSearchedObject<Customer>> searchResult, List<Integer> emailParamIdList, String email) {
        new OldParamSearchDAO(con).searchObjectListByEmail(TABLE_CUSTOMER, rs -> getCustomerFromRs(rs, "c."), searchResult, emailParamIdList, email);
    }

    /**
     * Selects customers by a text parameter
     * @param searchResult
     * @param textParamIdList
     * @param value
     * @throws SQLException
     */
    public void searchCustomerListByText(Pageable<Customer> searchResult, List<Integer> textParamIdList, String value) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Customer> list = searchResult.getList();

            StringBuilder query = new StringBuilder();
            String ids = Utils.toString(textParamIdList);

            query.append(SQL_SELECT);
            query.append("DISTINCT c.*");
            query.append(SQL_FROM);
            query.append(TABLE_CUSTOMER);
            query.append("AS c ");
            query.append(SQL_INNER_JOIN);
            query.append(Tables.TABLE_PARAM_TEXT);
            query.append("AS param ON c.id=param.id AND ");
            query.append("param.value LIKE ?");

            if (Utils.notBlankString(ids)) {
                query.append(" AND param.param_id IN (");
                query.append(ids);
                query.append(")");
            }
            query.append(SQL_ORDER_BY);
            query.append("c.title");
            query.append(page.getLimitSql());

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setString(1, value);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(getCustomerFromRs(rs, ""));
            }
            if (page != null) {
                page.setRecordCount(ps);
            }
            ps.close();
        }
    }

    /**
     * Selects customers by an address parameter
     * @param searchResult
     * @param addressParamIdList
     * @param streetId
     * @param house
     * @param houseFlat
     * @param houseRoom
     * @throws SQLException
     */
    public void searchCustomerListByAddress(Pageable<ParameterSearchedObject<Customer>> searchResult, List<Integer> addressParamIdList,
            int streetId, String house, String houseFlat, String houseRoom) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<ParameterSearchedObject<Customer>> list = searchResult.getList();

            PreparedQuery ps = new PreparedQuery(con);
            String ids = Utils.toString(addressParamIdList, "-1", ",");

            AddressHouse searchParams = new AddressHouse().withHouseAndFrac(house);

            int number = searchParams.getHouse();
            String frac = searchParams.getFrac();

            ps.addQuery(SQL_SELECT_COUNT_ROWS);
            ps.addQuery("DISTINCT param.param_id, param.value, c.* ");
            ps.addQuery(SQL_FROM);
            ps.addQuery(TABLE_CUSTOMER);
            ps.addQuery("AS c ");

            ps.addQuery(SQL_INNER_JOIN);
            ps.addQuery(Tables.TABLE_PARAM_ADDRESS);
            ps.addQuery("AS param ON c.id=param.id AND param.param_id IN (");
            ps.addQuery(ids);
            ps.addQuery(")");
            if (Utils.notBlankString(houseFlat)) {
                ps.addQuery(" AND param.flat=?");
                ps.addString(houseFlat);
            }
            if (Utils.notBlankString(houseRoom)) {
                ps.addQuery(" AND param.room=?");
                ps.addString(houseRoom);
            }

            ps.addQuery(SQL_INNER_JOIN);
            ps.addQuery(Tables.TABLE_ADDRESS_HOUSE);
            ps.addQuery("AS house ON param.house_id=house.id");
            ps.addQuery(" AND house.street_id=?");
            ps.addInt(streetId);
            if (number > 0) {
                ps.addQuery(" AND house.house=?");
                ps.addInt(number);
            }
            if (Utils.notBlankString(frac)) {
                ps.addQuery(" AND house.frac=?");
                ps.addString(frac);
            }

            ps.addQuery(SQL_LEFT_JOIN);
            ps.addQuery(Tables.TABLE_PARAM_PREF);
            ps.addQuery("AS pref ON param.param_id=pref.id ");

            ps.addQuery(SQL_ORDER_BY);
            ps.addQuery("c.title");
            ps.addQuery(page.getLimitSql());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ParameterSearchedObject<>(getCustomerFromRs(rs, "c."), rs.getInt(1), rs.getString(2)));
            }

            page.setRecordCount(ps.getPrepared());
            ps.close();
        }
    }

    /**
     * Selects customers by an address parameter
     * @param searchResult
     * @param addressParamIdList
     * @param houseId house code
     * @param houseFlat flat
     * @param houseRoom room
     */
    public void searchCustomerListByAddress(Pageable<ParameterSearchedObject<Customer>> searchResult, List<Integer> addressParamIdList,
            int houseId, String houseFlat, String houseRoom) {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<ParameterSearchedObject<Customer>> list = searchResult.getList();

            PreparedQuery ps = new PreparedQuery(con);
            String ids = Utils.toString(addressParamIdList);

            ps.addQuery(SQL_SELECT_COUNT_ROWS);
            ps.addQuery("DISTINCT param.param_id, param.value, c.* ");
            ps.addQuery(SQL_FROM);
            ps.addQuery(TABLE_CUSTOMER);
            ps.addQuery("AS c ");

            ps.addQuery(SQL_INNER_JOIN);
            ps.addQuery(Tables.TABLE_PARAM_ADDRESS);
            ps.addQuery("AS param ON c.id=param.id AND param.param_id IN (");
            ps.addQuery(ids);
            ps.addQuery(")");

            ps.addQuery(" AND param.house_id=?");
            ps.addInt(houseId);

            if (Utils.notBlankString(houseFlat)) {
                ps.addQuery(" AND param.flat=?");
                ps.addString(houseFlat);
            }
            if (Utils.notBlankString(houseRoom)) {
                ps.addQuery(" AND param.room=?");
                ps.addString(houseRoom);
            }

            ps.addQuery(SQL_ORDER_BY);
            ps.addQuery("c.title");
            ps.addQuery(page.getLimitSql());

            try (ps) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new ParameterSearchedObject<>(getCustomerFromRs(rs, "c."), rs.getInt(1), rs.getString(2)));
                }

                page.setRecordCount(ps.getPrepared());
            } catch (SQLException ex) {
                throw new BGException(ex);
            }
        }
    }

    /**
     * Selects customers by a phone number or numbers
     * @param searchResult
     * @param phoneParamIdList
     * @param phoneNumbers
     */
    public void searchCustomerListByPhone(Pageable<Customer> searchResult, Collection<Integer> phoneParamIdList, String... phoneNumbers)
            {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Customer> list = searchResult.getList();
            String ids = Utils.toString(phoneParamIdList);

            StringBuilder selectPart = new StringBuilder();
            StringBuilder joinPart = new StringBuilder();
            StringBuilder query = new StringBuilder();

            selectPart.append(SQL_SELECT);
            selectPart.append(" DISTINCT ");

            joinPart.append(SQL_INNER_JOIN);
            joinPart.append(Tables.TABLE_PARAM_PHONE_ITEM);
            joinPart.append(" AS p ON customer.id=p.id ");
            if (ids.length() > 0) {
                joinPart.append("AND p.param_id IN ( ");
                joinPart.append(ids);
                joinPart.append(" ) ");
            }

            // otherwise MySQL doesn't understand it can use the index
            for (int i = 0; i < phoneNumbers.length; i++) {
                phoneNumbers[i] = "'" + phoneNumbers[i] + "'";
            }

            joinPart.append(" AND p.phone IN (");
            joinPart.append(Utils.toString(Arrays.asList(phoneNumbers)));
            joinPart.append(")");

            String referenceTemplate = addCustomerReferenceQuery(selectPart, joinPart);

            query.append(selectPart);
            query.append(" customer.* ");
            query.append(SQL_FROM);
            query.append(TABLE_CUSTOMER);
            query.append(joinPart);
            query.append(SQL_WHERE);
            query.append("1=1 ");

            query.append(" ORDER BY customer.title ");
            query.append(page.getLimitSql());

            PreparedQuery ps = new PreparedQuery(con);
            ps.addQuery(query.toString());
            extractCustomersWithRef(page, list, referenceTemplate, ps);
            ps.close();
        }
    }

    /**
     * Selects a customer by its code
     * @param customerId
     * @return the customer
     */
    public Customer getCustomerById(int customerId) {
        Customer customer = null;

        try {
            String sql = "SELECT * FROM customer WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customer = getCustomerFromRs(rs, "");
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return customer;
    }

    /**
     * Updates customer information in DB
     * @param customer
     */
    public void updateCustomer(Customer customer) throws SQLException {
        if (customer != null) {
            int index = 1;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();

            if (customer.getId() > 0) {
                query.append(SQL_UPDATE);
                query.append(TABLE_CUSTOMER);
                query.append(" SET title=?, title_pattern=?, title_pattern_id=?, param_group_id=?");
                query.append(" WHERE id=?");
                ps = con.prepareStatement(query.toString());
                ps.setString(index++, customer.getTitle());
                ps.setString(index++, customer.getTitlePattern());
                ps.setInt(index++, customer.getTitlePatternId());
                ps.setInt(index++, customer.getParamGroupId());
                ps.setInt(index++, customer.getId());
                ps.executeUpdate();
            } else {
                query.append(SQL_INSERT_INTO);
                query.append(TABLE_CUSTOMER);
                query.append(" SET title=?, title_pattern=?, title_pattern_id=?, param_group_id=?,");
                query.append(" create_dt=now(), create_user_id=?");
                ps = con.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
                ps.setString(index++, customer.getTitle());
                ps.setString(index++, customer.getTitlePattern());
                ps.setInt(index++, customer.getTitlePatternId());
                ps.setInt(index++, customer.getParamGroupId());
                ps.setInt(index++, customer.getCreateUserId());
                ps.executeUpdate();
                customer.setId(lastInsertId(ps));
            }

            ps.close();
        }
    }

    /**
     * Deletes a customer from DB by code
     * @param id
     * @throws SQLException
     */
    public void deleteCustomer(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement("DELETE FROM " + TABLE_CUSTOMER + " WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Updates the customer's title, generating it from parameters
     * @param titleBefore the original title
     * @param customer the customer
     * @param changedParamId the changed parameter's code
     * @param response if passed, an event about the title change will be added there
     * @throws Exception
     */
    public void updateCustomerTitle(String titleBefore, Customer customer, int changedParamId, Response response) throws Exception {
        PatternDAO patternDAO = new PatternDAO(con);

        Customer oldCustomer = getCustomerById(customer.getId());
        oldCustomer.setGroupIds(this.getGroupIds(customer.getId()));

        try {
            // assume -1 means a personal pattern
            String titlePattern = customer.getTitlePattern();
            // no pattern
            if (customer.getTitlePatternId() == 0) {
                customer.setTitlePattern(titlePattern = "");
            }
            // pattern from the directory
            else if (customer.getTitlePatternId() > 0) {
                Pattern pattern = patternDAO.getPattern(customer.getTitlePatternId());
                if (pattern != null) {
                    titlePattern = pattern.getPattern();
                }
            }

            // generating the title from the pattern
            if (Utils.notBlankString(titlePattern) && (changedParamId < 0 || titlePattern.contains(String.valueOf(changedParamId)))) {
                customer.setTitle(PatternDAO.format(new ParamExpressionObject(con, customer.getId()), titlePattern));
            }

            if (oldCustomer != null) {
                logCustomerChange(customer, oldCustomer);
            }

            updateCustomer(customer);

            boolean changed = !titleBefore.equals(customer.getTitle());
            if (changed && response != null) {
                response.addEvent(new CustomerTitleChangedEvent(customer.getId(), customer.getTitle()));
            }

            if (changed) {
                // updating the customer's title in process links
                new ProcessLinkDAO(con).updateLinkTitles(customer.getId(), Customer.OBJECT_TYPE + "%", customer.getTitle());
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Returns customer titles containing a substring
     * @param title the substring, search uses a LIKE expression
     * @param count count of the first titles
     * @return the titles
     */
    public List<String> getCustomerTitles(String title, int count) {
        List<String> result = new ArrayList<>();

        try {
            String query = " SELECT title FROM " + TABLE_CUSTOMER + " WHERE title LIKE ? " + " GROUP BY title ORDER BY title LIMIT ?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, title);
            ps.setInt(2, count);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public Set<Integer> getGroupIds(int customerId) throws SQLException {
        return getIds(TABLE_CUSTOMER_GROUP, "customer_id", "group_id", customerId);
    }

    public void updateGroupIds(int customerId, Set<Integer> groupIds) throws SQLException {
        updateIds(TABLE_CUSTOMER_GROUP, "customer_id", "group_id", customerId, groupIds);
    }

    /**
     * Returns a created {@link Customer} object filled from {@link ResultSet}
     * @param rs
     * @param prefix
     * @throws SQLException
     */
    public static Customer getCustomerFromRs(ResultSet rs, String prefix) throws SQLException {
        Customer customer = new Customer();

        customer.setId(rs.getInt(prefix + "id"));
        customer.setTitle(rs.getString(prefix + "title"));
        customer.setTitlePattern(rs.getString(prefix + "title_pattern"));
        customer.setTitlePatternId(rs.getInt(prefix + "title_pattern_id"));
        customer.setParamGroupId(rs.getInt(prefix + "param_group_id"));
        customer.setCreateTime(rs.getTimestamp(prefix + "create_dt"));
        customer.setCreateUserId(rs.getInt(prefix + "create_user_id"));
        customer.setPassword(rs.getString(prefix + "pswd"));

        return customer;
    }

    private void logCustomerChange(Customer customer, Customer oldCustomer) throws SQLException {
        if (history) {
            new CustomerLogDAO(con).insertEntityLog(customer.getId(), userId, customer.toLog(con, oldCustomer));
        }
    }

    // deprecated

    @Deprecated
    public void searchCustomerList(Pageable<Customer> searchResult, Set<Integer> groupIds) {
        log.warndMethod("searchCustomerList");

        Page page = searchResult.getPage();
        List<Customer> list = searchResult.getList();

        StringBuilder selectPart = new StringBuilder();
        StringBuilder joinPart = new StringBuilder();

        String referenceTemplate = addCustomerReferenceQuery(selectPart, joinPart);

        if (CollectionUtils.isNotEmpty(groupIds)) {
            joinPart.append(" INNER JOIN " + TABLE_CUSTOMER_GROUP + " AS customer_group ON customer.id=customer_group.customer_id "
                    + "AND customer_group.group_id IN (" + Utils.toString(groupIds) + ") ");

            PreparedQuery ps = new PreparedQuery(con);

            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT_COUNT_ROWS + " DISTINCT ");
            query.append(selectPart);
            query.append("customer.* FROM " + TABLE_CUSTOMER + " AS customer");
            query.append(joinPart);
            query.append(" ORDER BY title");
            query.append(page.getLimitSql());

            ps.addQuery(query.toString());

            extractCustomersWithRef(page, list, referenceTemplate, ps);
        }

        PreparedQuery ps = new PreparedQuery(con);

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT_COUNT_ROWS + " DISTINCT ");
        query.append(selectPart);
        query.append("customer.* FROM " + TABLE_CUSTOMER + " AS customer");
        query.append(joinPart);
        query.append(" ORDER BY title");
        query.append(page.getLimitSql());

        log.debug(query.toString());

        ps.addQuery(query.toString());

        extractCustomersWithRef(page, list, referenceTemplate, ps);
    }

    @Deprecated
    public Customer extractCustomerWithRef(int customerId) {
        log.warndMethod("extractCustomerWithRef");

        try {
            int index = 1;
            StringBuilder query = new StringBuilder();
            StringBuilder selectPart = new StringBuilder();
            StringBuilder joinPart = new StringBuilder();

            String referenceTemplate = addCustomerReferenceQuery(selectPart, joinPart);

            query.append(SQL_SELECT);
            query.append(selectPart);
            query.append(" customer.* ");
            query.append(SQL_FROM);
            query.append(TABLE_CUSTOMER);
            query.append(" AS customer");
            query.append(joinPart);
            query.append(" WHERE customer.id = ? ");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(index++, customerId);

            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (Utils.notBlankString(referenceTemplate)) {
                    Customer customer = getCustomerFromRs(rs, "");

                    String reference = PatternFormatter.processPattern(referenceTemplate, variable -> {
                        String value = "";
                        try {
                            if (variable.startsWith("param:")) {
                                value = rs.getString(variable.replace(':', '_') + "_val");
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        return value;
                    });

                    ps.close();
                    customer.setReference(reference);
                    return customer;
                }
            }
            ps.close();
            return null;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    @Deprecated
    public void searchCustomerByLinkedObjectTitle(Pageable<Customer> searchResult, String linkedObjectTypeLike, String linkedObjectTitle) {
        log.warndMethod("searchCustomerByLinkedObjectTitle");

        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Customer> list = searchResult.getList();

            StringBuilder selectPart = new StringBuilder();
            StringBuilder joinPart = new StringBuilder();
            StringBuilder query = new StringBuilder();

            selectPart.append(SQL_SELECT);
            selectPart.append(" DISTINCT ");

            joinPart.append(SQL_INNER_JOIN);
            joinPart.append(TABLE_CUSTOMER_LINK);
            joinPart.append(" AS link ON link.customer_id = customer.id ");
            joinPart.append("AND link.object_title LIKE ? ");
            joinPart.append("AND link.object_type LIKE ? ");

            String referenceTemplate = addCustomerReferenceQuery(selectPart, joinPart);

            query.append(selectPart);
            query.append(" customer.* ");
            query.append(SQL_FROM);
            query.append(TABLE_CUSTOMER);
            query.append(joinPart);

            query.append(" ORDER BY customer.title ");
            query.append(page.getLimitSql());

            PreparedQuery ps = new PreparedQuery(con);
            ps.addQuery(query.toString());
            ps.addString(linkedObjectTitle);
            ps.addString(linkedObjectTypeLike);
            extractCustomersWithRef(page, list, referenceTemplate, ps);
            ps.close();
        }
    }

    @Deprecated
    public Customer getCustomerByTitle(String customerTitle) throws SQLException {
        log.warndMethod("getCustomerByTitle");

        Customer customer = null;

        int index = 1;

        PreparedStatement ps = con.prepareStatement("SELECT * FROM customer WHERE UPPER(title)=?");
        ps.setString(index++, customerTitle.toUpperCase());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            customer = getCustomerFromRs(rs, "");
        }
        ps.close();

        return customer;
    }
}
