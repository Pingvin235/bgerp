package org.bgerp.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Fluent process search DAO.
 *
 * @author Shamil Vakhitov
 */
public class ProcessSearchDAO extends SearchDAO {
    private Set<Integer> excludeIds;
    private String idOrDescriptionLike;

    /**
     * {@inheritDoc}
     */
    public ProcessSearchDAO(Connection con) {
        super(con);
    }

    /**
     * {@inheritDoc}
     */
    public ProcessSearchDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessSearchDAO withOpen(Boolean value) {
        return (ProcessSearchDAO) super.withOpen(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessSearchDAO withType(Set<Integer> value) {
        return (ProcessSearchDAO) super.withType(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessSearchDAO withStatus(Set<Integer> values) {
        return (ProcessSearchDAO) super.withStatus(values);
    }

    /**
     * Excluded process IDs
     * @param values the process IDs
     * @return
     */
    public ProcessSearchDAO withoutId(Set<Integer> values) {
        this.excludeIds = values;
        return this;
    }

    /**
     * SQL LIKE expression for id or description.
     * @param value the LIKE expression.
     * @return
     */
    public ProcessSearchDAO withIdOrDescriptionLike(String value) {
        this.idOrDescriptionLike = value;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessSearchDAO order(Order value) {
        return (ProcessSearchDAO) super.order(value);
    }

    /**
     * Queries processes.
     * @param result pageable result.
     * @throws SQLException
     */
    public void search(Pageable<Process> result) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            var page = result.getPage();
            var list = result.getList();

            pq.addQuery(SQL_SELECT_COUNT_ROWS + "p.*" + SQL_FROM + TABLE_PROCESS + "AS p");

            pq.addQuery(SQL_WHERE + "1>0 ");

            filterOpen(pq);
            filterType(pq);
            filterStatus(pq);

            if (CollectionUtils.isNotEmpty(excludeIds))
                pq.addQuery(SQL_AND + "p.id NOT IN (" + Utils.toString(excludeIds) + ") ");

            if (Utils.notBlankString(idOrDescriptionLike)) {
                pq.addQuery(SQL_AND + "(p.id LIKE ? OR p.description LIKE ?)");
                pq.addString(idOrDescriptionLike).addString(idOrDescriptionLike);
            }

            order(pq);

            pq.addQuery(page.getLimitSql());

            var rs = pq.executeQuery();
            while (rs.next())
                list.add(ProcessDAO.getProcessFromRs(rs, "p."));

            page.setRecordCount(pq.getPrepared());
        }
    }
}
