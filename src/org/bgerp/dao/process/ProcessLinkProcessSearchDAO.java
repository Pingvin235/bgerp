package org.bgerp.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.sql.Connection;
import java.sql.SQLException;

import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Fluent DAO for selection parent or child linked processes.
 *
 * @author Shamil Vakhitov
 */
public class ProcessLinkProcessSearchDAO extends ProcessSearchDAO {
    private static final String LIKE_PROCESS = " LIKE 'process%'";

    /**
     * {@inheritDoc}
     */
    public ProcessLinkProcessSearchDAO(Connection con) {
        super(con);
    }

    /**
     * {@inheritDoc}
     */
    public ProcessLinkProcessSearchDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessLinkProcessSearchDAO withOpen(Boolean value) {
        return (ProcessLinkProcessSearchDAO) super.withOpen(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessLinkProcessSearchDAO order(Order value) {
        return (ProcessLinkProcessSearchDAO) super.order(value);
    }

    /**
     * Queries processes.
     * @param result pageable result, first pair's param is {@link ru.bgcrm.model.CommonObjectLink#getObjectType()}.
     * @param link {@code true} - child relations, {@code false} - parent.
     * @param processId process ID.
     * @throws SQLException
     */
    public void search(Pageable<Pair<String, Process>> result, boolean link, int processId) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            var page = result.getPage();
            var list = result.getList();

            pq.addQuery(SQL_SELECT_COUNT_ROWS + SQL_DISTINCT + "l.object_type, p.*" + SQL_FROM + TABLE_PROCESS + "AS p");
            pq.addQuery(SQL_INNER_JOIN + TABLE_PROCESS_LINK + "AS l ON ");

            if (link)
                pq.addQuery("p.id=l.object_id AND l.process_id=? AND l.object_type" + LIKE_PROCESS);
            else
                pq.addQuery("p.id=l.process_id AND l.object_id=? AND l.object_type" + LIKE_PROCESS);

            pq.addInt(processId);
            pq.addQuery(ProcessDAO.getIsolationJoin(form, "p"));

            pq.addQuery(SQL_WHERE + "1>0 ");
            filterOpen(pq);

            order(pq);

            pq.addQuery(getPageLimit(page));

            var rs = pq.executeQuery();
            while (rs.next())
                list.add(new Pair<String, Process>(rs.getString(1), ProcessDAO.getProcessFromRs(rs, "p.")));

            page.setRecordCount(foundRows(pq.getPrepared()));
        }
    }
}
