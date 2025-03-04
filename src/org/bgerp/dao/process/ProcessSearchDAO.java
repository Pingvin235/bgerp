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

/**
 * Fluent process search DAO.
 *
 * @author Shamil Vakhitov
 */
public class ProcessSearchDAO extends SearchDAO {
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
     * {@inheritDoc}
     */
    @Override
    public ProcessSearchDAO withExecutor(Set<Integer> values) {
        return (ProcessSearchDAO) super.withExecutor(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessSearchDAO withoutId(Set<Integer> values) {
        return (ProcessSearchDAO) super.withoutId(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessSearchDAO withIdOrDescriptionLike(String value) {
        return (ProcessSearchDAO) super.withIdOrDescriptionLike(value);
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

            pq.addQuery(SQL_SELECT_COUNT_ROWS);
            if (CollectionUtils.isNotEmpty(executorIds))
                pq.addQuery(SQL_DISTINCT);
            pq.addQuery("p.*" + SQL_FROM + TABLE_PROCESS + "AS p");

            filterExecutor(pq);

            pq.addQuery(SQL_WHERE + "1>0 ");

            filterOpen(pq);
            filterType(pq);
            filterStatus(pq);

            filterId(pq);

            filterIdOrDescriptionLike(pq);

            order(pq);

            pq.addQuery(page.getLimitSql());

            var rs = pq.executeQuery();
            while (rs.next())
                list.add(ProcessDAO.getProcessFromRs(rs, "p."));

            page.setRecordCount(pq.getPrepared());
        }
    }
}
