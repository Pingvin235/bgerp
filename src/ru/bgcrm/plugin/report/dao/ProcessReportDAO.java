package ru.bgcrm.plugin.report.dao;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.PreparedDelay;
import ru.bgerp.util.Log;

public class ProcessReportDAO extends ReportDAO {
    private static final Log log = Log.getLog();
    
    @Override
    public String getJspFile() {
        return "/WEB-INF/jspf/user/plugin/report/report/process.jsp";
    }

    @Override
    public void get(DynActionForm form) throws Exception {
        Date dateFrom = form.getParamDate("dateFrom", new Date(), true);
        Date dateTo = form.getParamDate("dateTo", new Date(), true);

        String type = form.getParam("type", "create", true);
        if (!StringUtils.equalsAny(type, "create", "close"))
            throw new BGIllegalArgumentException();
        
        try (Connection con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            SearchResult<Object[]> result = new SearchResult<>(form);
            
            PreparedDelay pd = new PreparedDelay(con);
            pd.addQuery(SQL_SELECT_COUNT_ROWS + "id, type_id, " + type + "_user_id, description" + SQL_FROM + TABLE_PROCESS + SQL_WHERE);
            pd.addQuery(type + "_dt");
            pd.addQuery(" BETWEEN ? AND ?");
            pd.addDate(dateFrom);
            pd.addDate(TimeUtils.getNextDay(dateTo));
            pd.addQuery(SQL_ORDER_BY);
            pd.addQuery(type + "_user_id");
            
            ResultSet rs = pd.executeQuery();
            while (rs.next()) {
                // TODO: Keep in mind necessity of retrieving file uploadable data in future. 
                Object[] row = new Object[4];
                row[0] = rs.getInt(1);
                row[1] = ProcessTypeCache.getProcessType(rs.getInt(2));
                row[2] = UserCache.getUser(rs.getInt(3));
                row[3] = rs.getString(4);
                result.getList().add(row);
            }
            
            setRecordCount(form.getPage(), pd.getPrepared());
        }
    }

}
