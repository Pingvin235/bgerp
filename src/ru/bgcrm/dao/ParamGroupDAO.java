package ru.bgcrm.dao;

import java.sql.Connection;

/**
 * @see org.bgerp.dao.param.ParamGroupDAO
 */
@Deprecated
public class ParamGroupDAO extends org.bgerp.dao.param.ParamGroupDAO {
    public ParamGroupDAO(Connection con) {
        super(con);
    }
}
