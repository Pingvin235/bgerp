package ru.bgcrm.dao;

import java.sql.Connection;

/**
 * @see org.bgerp.dao.param.ParamValueDAO
 */
@Deprecated
public class ParamValueDAO extends org.bgerp.dao.param.ParamValueDAO {
    public ParamValueDAO(Connection con) {
        super(con);
    }
}
