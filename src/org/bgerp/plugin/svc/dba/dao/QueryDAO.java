package org.bgerp.plugin.svc.dba.dao;

import java.sql.SQLException;
import java.util.regex.Pattern;

import org.bgerp.plugin.svc.dba.model.QueryTable;
import org.bgerp.plugin.svc.dba.model.QueryType;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * SQL query execution DAO.
 *
 * @author Shamil Vakhitov
 */
public class QueryDAO extends CommonDAO {
    private static final Pattern PATTERN_LIMIT = Pattern.compile( "(?i)limit((,)?\\s*\\d+\\s*){1,2}");

    private final ConnectionSet conSet;

    public QueryDAO(ConnectionSet conSet) {
        this.conSet = conSet;
    }

    /**
     * Executes SQL query.
     * @param table resulting table with pagination.
     * @param type type.
     * @param query the query.
     * @throws SQLException
     */
    public void query(QueryTable table, QueryType type, String query) throws SQLException {
        query = query.trim().toLowerCase();

        Page page = table.getPage();

        if (type == QueryType.SELECT) {
            query = query.replaceFirst("(?i)select", SQL_SELECT_COUNT_ROWS);
            if (!PATTERN_LIMIT.matcher(query).find()) {
                query += getPageLimit(page);
            }
        }

        try (var st = conSet.getConnection().createStatement()) {
            st.execute(query);

            if (type == QueryType.SELECT || type == QueryType.SHOW) {
                table.set(st.getResultSet());

                if (type == QueryType.SELECT)
                    page.setRecordCount(foundRows(st));
            } else {
                table.set("UpdateCount", String.valueOf(st.getUpdateCount()));
            }
        }
    }
}
