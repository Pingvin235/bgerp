package ru.bgcrm.dao;

import java.sql.Connection;

import org.bgerp.util.Log;

/**
 * @see org.bgerp.dao.param.ParamValueDAO
 */
@Deprecated
public class ParamValueDAO extends org.bgerp.dao.param.ParamValueDAO {
    private static final Log log = Log.getLog();

    public ParamValueDAO(Connection con) {
        super(con);
        log.warndClass(ParamValueDAO.class, org.bgerp.dao.param.ParamValueDAO.class);
    }
}
