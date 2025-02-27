package org.bgerp.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LOG;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bgerp.app.l10n.Localizer;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.model.Pageable;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.EntityLogDAO;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.model.EntityLogItem;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.util.Utils;

public class ProcessLogDAO extends EntityLogDAO {
    public ProcessLogDAO(Connection con) {
        super(con, Tables.TABLE_PROCESS_LOG);
    }

    /**
     * Selects process change logs.
     * @param l localizer to translate some strings.
     * @param processType
     * @param processId
     * @param result
     * @throws SQLException
     */
    public void searchProcessLog(Localizer l, ProcessType processType, int processId, Pageable<EntityLogItem> result) throws SQLException {
        Page page = result.getPage();

        PreparedQuery pq = new PreparedQuery(con);

        pq.addQuery(SQL_SELECT_COUNT_ROWS + " dt, user_id, 0, data, 0" + SQL_FROM + TABLE_PROCESS_LOG);
        pq.addQuery(SQL_WHERE + "id=?");
        pq.addInt(processId);

        pq.addQuery(" UNION SELECT dt, user_id, -1, status_id, comment" + SQL_FROM + TABLE_PROCESS_STATUS);
        pq.addQuery(SQL_WHERE + "process_id=?");
        pq.addInt(processId);

        pq.addQuery(" UNION SELECT dt, user_id, param_id, text, 0" + SQL_FROM + org.bgerp.dao.param.Tables.TABLE_PARAM_LOG);
        pq.addQuery(SQL_WHERE + "object_id=? AND param_id IN ( "
                + Utils.toString(processType.getProperties().getParameterIds(), " 0 ", " , ") + " )");
        pq.addInt(processId);

        pq.addQuery(SQL_ORDER_BY + "dt DESC ");
        pq.addQuery(getPageLimit(page));

        List<EntityLogItem> list = result.getList();
        try (pq) {
            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                String text = " ??? ";
                int paramId = rs.getInt(3);
                // param
                if (paramId > 0) {
                    Parameter param = ParameterCache.getParameter(paramId);
                    if (param != null)
                        text = l.l("Параметр") + " '" + param.getTitle() + "': " + rs.getString(4);
                }
                // status
                else if (paramId == -1) {
                    Status status = ProcessTypeCache.getStatusMap().get(Utils.parseInt(rs.getString(4)));
                    text = l.l("Status") + ": " + (status != null ? status.getTitle() : " ??? " + rs.getString(4) + " ") + " ["
                            + Utils.maskNull(rs.getString(5)) + "]";
                }
                // process
                else {
                    text = rs.getString(4);
                }
                list.add(new EntityLogItem(rs.getTimestamp(1), processId, rs.getInt(2), text));
            }
            page.setRecordCount(pq.getPrepared());
        }
    }
}
