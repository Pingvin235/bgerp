package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER_LINK;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.customer.Customer;

/**
 * Customer links used for now only for storing linked contract for plugin BGBilling.
 */
public class CustomerLinkDAO extends CommonLinkDAO {
    public CustomerLinkDAO(Connection con) {
        super(con);
    }

    @Override
    protected String getTable() {
        return TABLE_CUSTOMER_LINK;
    }

    @Override
    protected String getColumnName() {
        return "customer_id";
    }

    @Override
    protected String getObjectType() {
        return Customer.OBJECT_TYPE;
    }

    public void searchCustomerByLink(Pageable<Customer> searchResult, CommonObjectLink link) throws BGException {
        Page page = searchResult.getPage();
        List<Customer> list = searchResult.getList();

        try {
            String query = "SELECT * FROM " + TABLE_CUSTOMER_LINK + " AS link " + "INNER JOIN " + TABLE_CUSTOMER + " AS c ON link.customer_id=c.id "
                    + "WHERE link.object_id=? AND link.object_type=? " + getPageLimit(page);

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, link.getLinkObjectId());
            ps.setString(2, link.getLinkObjectType());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(CustomerDAO.getCustomerFromRs(rs, ""));
            }
            if (page != null) {
                page.setRecordCount(foundRows(ps));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }
}