package org.bgerp.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.sql.Connection;
import java.sql.SQLException;

import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Fluent DAO for selection processes by links.
 *
 * @author Shamil Vakhitov
 */
public class ProcessLinkSearchDAO extends SearchDAO {
    private String linkObjectType;
    private int linkObjectId;

    /**
     * {@inheritDoc}
     */
    public ProcessLinkSearchDAO(Connection con) {
        super(con);
    }

    /**
     * {@inheritDoc}
     */
    public ProcessLinkSearchDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessLinkSearchDAO withOpen(Boolean value) {
        return (ProcessLinkSearchDAO) super.withOpen(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessLinkSearchDAO order(Order value) {
        return (ProcessLinkSearchDAO) super.order(value);
    }

    /**
     * Filter by link object type.
     * @param value
     * @return
     */
    public ProcessLinkSearchDAO withLinkObjectType(String value) {
        this.linkObjectType = value;
        return this;
    }

    /**
     * Filter by link object ID.
     * @param value
     * @return
     */
    public ProcessLinkSearchDAO withLinkObjectId(int value) {
        this.linkObjectId = value;
        return this;
    }

    /**
     * Queries processes.
     * @param result pageable result.
     * throws SQLException
     */
    public void search(Pageable<Process> result) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            var page = result.getPage();
            var list = result.getList();

            pq.addQuery(SQL_SELECT_COUNT_ROWS + SQL_DISTINCT + "p.*" + SQL_FROM + TABLE_PROCESS + "AS p");
            pq.addQuery(SQL_INNER_JOIN + TABLE_PROCESS_LINK + "AS l ON ");
            pq.addQuery("p.id=l.process_id");

            if (Utils.notBlankString(linkObjectType))
                pq.addQuery(SQL_AND + "l.object_type=?").addString(linkObjectType);

            if (linkObjectId > 0)
                pq.addQuery(SQL_AND + "l.object_id=?").addInt(linkObjectId);

            pq.addQuery(ProcessDAO.getIsolationJoin(form, "p"));

            pq.addQuery(SQL_WHERE + "1>0 ");
            filterOpen(pq);

            order(pq);

            pq.addQuery(page.getLimitSql());

            var rs = pq.executeQuery();
            while (rs.next())
                list.add(ProcessDAO.getProcessFromRs(rs, "p."));

            page.setRecordCount(pq.getPrepared());
        }
    }

}