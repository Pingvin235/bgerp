package org.bgerp.dao.process;

import java.sql.Connection;
import java.util.Collection;
import java.util.Set;

import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
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
    private Collection<Integer> statusIds;
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
     * Filter by process closing date.
     * @param value {@code null} - no filter, or process closing date not null.
     * @return
     */
    public SearchDAO withOpen(Boolean value) {
        this.open = value;
        return this;
    }

    /**
     * Filter by process type ID.
     * @param value {@code null} or empty - no filter, or set with type IDs.
     * @return
     */
    public SearchDAO withType(Set<Integer> value) {
        this.typeIds = value;
        return this;
    }

    /**
     * Filter by process status ID.
     * @param value {@code null} or empty - no filter, or set with status IDs.
     * @return
     */
    public SearchDAO withStatus(Collection<Integer> value) {
        this.statusIds = value;
        return this;
    }

    /**
     * Selection order.
     * @param value enum value.
     * @return
     */
    public SearchDAO order(Order value) {
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
        if (typeIds != null && !typeIds.isEmpty()) {
            pq.addQuery(SQL_AND + "p.type_id IN (");
            pq.addQuery(Utils.toString(typeIds));
            pq.addQuery(") ");
        }
    }

    protected void filterStatus(PreparedQuery pq) {
        if (statusIds != null && !statusIds.isEmpty()) {
            pq.addQuery(SQL_AND + "p.status_id IN (");
            pq.addQuery(Utils.toString(statusIds));
            pq.addQuery(") ");
        }
    }

    protected void order(PreparedQuery pq) {
        if (order != null) {
            pq.addQuery(SQL_ORDER_BY);
            pq.addQuery(order.sql("p."));
        }
    }
}
