package org.bgerp.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Fluent DAO for selection parent or child linked processes.
 *
 * @author Shamil Vakhitov
 */
public class ProcessLinkProcessSearchDAO extends ProcessSearchDAO {
    private Set<String> linkType;

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
    public ProcessLinkProcessSearchDAO withType(Set<Integer> value) {
        return (ProcessLinkProcessSearchDAO) super.withType(value);
    }

    /**
     * Filter by process to process link type.
     * @param value set of values from {@link Process#LINK_TYPE_DEPEND}, {@link Process#LINK_TYPE_LINK}, {@link Process#LINK_TYPE_MADE}.
     */
    public ProcessLinkProcessSearchDAO withLinkType(Set<String> value) {
        this.linkType = value;
        return this;
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

            String linkTypeCompare = " LIKE 'process%'";
            if (linkType != null)
                linkTypeCompare = " IN ('" + Utils.toString(linkType, "", "','") + "')";

            if (link)
                pq.addQuery("p.id=l.object_id AND l.process_id=? AND l.object_type" + linkTypeCompare);
            else
                pq.addQuery("p.id=l.process_id AND l.object_id=? AND l.object_type" + linkTypeCompare);

            pq.addInt(processId);
            pq.addQuery(ProcessDAO.getIsolationJoin(form, "p"));

            pq.addQuery(SQL_WHERE + "1>0 ");
            filterOpen(pq);
            filterType(pq);

            order(pq);

            pq.addQuery(getPageLimit(page));

            var rs = pq.executeQuery();
            while (rs.next())
                list.add(new Pair<>(rs.getString(1), ProcessDAO.getProcessFromRs(rs, "p.")));

            page.setRecordCount(pq.getPrepared());
        }
    }
}
