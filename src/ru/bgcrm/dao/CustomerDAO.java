package ru.bgcrm.dao;

import java.sql.Connection;

@Deprecated
public class CustomerDAO extends org.bgerp.dao.customer.CustomerDAO {
    public CustomerDAO(Connection con) {
        super(con);
        log.warndClass(this.getClass(), org.bgerp.dao.customer.CustomerDAO.class);
    }

    public CustomerDAO(Connection con, boolean history, int userId) {
        super(con, history, userId);
        log.warndClass(this.getClass(), org.bgerp.dao.customer.CustomerDAO.class);
    }
}
