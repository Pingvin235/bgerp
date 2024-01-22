package ru.bgcrm.dao;

import java.sql.Connection;

/**
 * @see org.bgerp.dao.param.ParamLogDAO
 */
@Deprecated
public class ParamLogDAO extends org.bgerp.dao.param.ParamLogDAO {
    public ParamLogDAO(Connection con) {
        super(con);
    }
}
