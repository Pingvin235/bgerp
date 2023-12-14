package org.bgerp.dao.message.process;

import java.util.List;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.process.link.ProcessLink;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.user.User;

/**
 * Search types of possible processes.
 */
public abstract class MessagePossibleProcessSearch extends CommonDAO {
    private final int id;
    /** Optionally color. */
    private final String color;

    protected MessagePossibleProcessSearch(int id, ConfigMap configMap) {
        this.id = id;
        this.color = configMap.get("color");
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public abstract boolean addQuery(User user, PreparedQuery pq, boolean first, String from, List<ProcessLink> links, Boolean open);

    protected void addOpenFilter(PreparedQuery pq, Boolean open) {
        if (open != null) {
            if (open)
                pq.addQuery(SQL_WHERE + "close_dt IS NULL ");
            else
                pq.addQuery(SQL_WHERE + "close_dt IS NOT NULL ");
        }
    }
}