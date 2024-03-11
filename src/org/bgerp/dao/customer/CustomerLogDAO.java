package org.bgerp.dao.customer;

import java.sql.Connection;

import ru.bgcrm.dao.EntityLogDAO;
import ru.bgcrm.dao.Tables;

public class CustomerLogDAO extends EntityLogDAO {
    public CustomerLogDAO(Connection con) {
        super(con, Tables.TABLE_CUSTOMER_LOG);
    }
}
