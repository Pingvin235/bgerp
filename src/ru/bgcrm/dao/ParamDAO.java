package ru.bgcrm.dao;

import java.sql.Connection;

/**
 * @see org.bgerp.dao.param.ParamDAO
 */
@Deprecated
public class ParamDAO extends org.bgerp.dao.param.ParamDAO {
    public ParamDAO(Connection con) {
        super(con);
    }
}
