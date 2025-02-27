package org.bgerp.dao.customer;

import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.dao.param.Tables;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.util.Utils;

/**
 * Fluent DAO for selection customers by parameter values.
 *
 * @author Shamil Vakhitov
 */

public class CustomerParamSearchDAO extends CommonDAO {
    private Set<Integer> paramTextIds;
    private String paramTextValue;

    /**
     * {@inheritDoc}
     */
    public CustomerParamSearchDAO(Connection con) {
        super(con);
    }

    public CustomerParamSearchDAO withParamTextIds(Set<Integer> values) {
        this.paramTextIds = values;
        return this;
    }

    public CustomerParamSearchDAO withParamTextValue(String value) {
        this.paramTextValue = value;
        return this;
    }

    /**
     * Queries customers with param values.
     * @param result pageable result.
     * @throws SQLException
     */
    public void search(Pageable<ParameterSearchedObject<Customer>> result) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            var page = result.getPage();
            var list = result.getList();

            pq.addQuery(SQL_SELECT_COUNT_ROWS + "c.*, param.*" + SQL_FROM + TABLE_CUSTOMER + "AS c");

            if (Utils.notBlankString(paramTextValue)) {
                pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_PARAM_TEXT + "AS param ON c.id=param.id AND param.value LIKE ?");
                pq.addString(paramTextValue);
            } else
                throw new IllegalArgumentException("No param value filter was defined.");

            pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_PARAM_PREF + "AS param_pref ON param.param_id=param_pref.id AND param_pref.object=?");
            pq.addString(Customer.OBJECT_TYPE);

            if (CollectionUtils.isNotEmpty(paramTextIds))
                pq.addQuery(SQL_AND + "param.param_id IN (").addQuery(Utils.toString(paramTextIds)).addQuery(")");

            pq.addQuery(SQL_WHERE + "1>0 ");

            pq.addQuery(SQL_ORDER_BY + "c.title");

            pq.addQuery(page.getLimitSql());

            var rs = pq.executeQuery();
            while (rs.next())
                list.add(new ParameterSearchedObject<>(
                        CustomerDAO.getCustomerFromRs(rs, "c."),
                        rs.getInt("param.param_id"),
                        rs.getString("param.value")));

            page.setRecordCount(pq.getPrepared());
        }
    }
}
