package org.bgerp.dao.process;

import java.sql.Connection;
import java.util.Set;

import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Basic process searching fluent DAO.
 * Extending that assuming using {@code p} as a synonym of {@link ru.bgcrm.dao.process.Tables#TABLE_PROCESS} table in SQL.
 *
 * @author Shamil Vakhitov
 */
abstract class SearchDAO extends CommonDAO {
    /** User request context for isolations. */
    protected final DynActionForm form;

    private Boolean open;
    private Set<Integer> typeIds;
    private Set<Integer> statusIds;
    private Set<Integer> executorIds;
    private Order order;

     /**
     * Constructor without isolations support.
     * @param con
     */
    protected SearchDAO(Connection con) {
        super(con);
        this.form = null;
    }

    /**
     * Constructor with isolations support.
     * @param con
     * @param form
     */
    protected SearchDAO(Connection con, DynActionForm form) {
        super(con);
        this.form = form;
    }

    /**
     * Filter by process closing date
     * @param value {@code null} - no filter, or process closing date is not null
     * @return
     */
    protected SearchDAO withOpen(Boolean value) {
        this.open = value;
        return this;
    }

    /**
     * Filter by process type ID
     * @param values {@code null} or empty - no filter, or set with type IDs
     * @return
     */
    protected SearchDAO withType(Set<Integer> values) {
        this.typeIds = values;
        return this;
    }

    /**
     * Filter by process status ID
     * @param values {@code null} or empty - no filter, or set with status IDs
     * @return
     */
    protected SearchDAO withStatus(Set<Integer> values) {
        this.statusIds = values;
        return this;
    }

    /**
     * Filter by process executors
     * @param values {@code null} or empty - no filter, or set with executor user IDs
     * @return
     */
    protected SearchDAO withExecutor(Set<Integer> values) {
        this.executorIds = values;
        return this;
    }

    /**
     * Selection order.
     * @param value enum value.
     * @return
     */
    protected SearchDAO order(Order value) {
        this.order = value;
        return this;
    }

    protected void filterOpen(PreparedQuery pq) {
        if (open != null) {
            pq.addQuery(SQL_AND + "p.close_dt IS ");
            if (!open)
                pq.addQuery("NOT ");
            pq.addQuery("NULL ");
        }
    }

    protected void filterType(PreparedQuery pq) {
        if (typeIds != null && !typeIds.isEmpty())
            pq.addQuery(SQL_AND + "p.type_id IN (").addQuery(Utils.toString(typeIds)).addQuery(") ");
    }

    protected void filterStatus(PreparedQuery pq) {
        if (statusIds != null && !statusIds.isEmpty())
            pq.addQuery(SQL_AND + "p.status_id IN (").addQuery(Utils.toString(statusIds)).addQuery(") ");
    }

    protected void filterExecutor(PreparedQuery pq) {
        if (executorIds != null && !executorIds.isEmpty())
            pq.addQuery(SQL_INNER_JOIN + Tables.TABLE_PROCESS_EXECUTOR + "AS pe ON p.id=pe.process_id AND pe.user_id IN (").addQuery(Utils.toString(executorIds)).addQuery(") ");
    }

    protected void order(PreparedQuery pq) {
        if (order != null)
            pq.addQuery(SQL_ORDER_BY).addQuery(order.sql("p."));
    }
}
