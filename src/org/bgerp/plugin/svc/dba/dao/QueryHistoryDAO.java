package org.bgerp.plugin.svc.dba.dao;

import static org.bgerp.plugin.svc.dba.dao.Tables.QUERY_HISTORY_TABLE;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.IdTitle;

public class QueryHistoryDAO extends CommonDAO {
    public QueryHistoryDAO(Connection con) {
        super(con);
    }

    /**
     * Inserts query in history log or updates last usage time.
     * @param userId user ID.
     * @param query the query.
     * @throws SQLException
     */
    public void update(int userId, String query) throws SQLException {
        query = query.trim();

        int rowsUpdated;
        try (var pq = new PreparedQuery(con, SQL_UPDATE + QUERY_HISTORY_TABLE + "SET last_dt=NOW() WHERE user_id=? AND data=?")) {
            pq.addInt(userId);
            pq.addString(query);

            rowsUpdated = pq.executeUpdate();
        }

        if (rowsUpdated == 0) {
            try (var pq = new PreparedQuery(con, SQL_INSERT + QUERY_HISTORY_TABLE + "SET user_id=?, data=?")) {
                pq.addInt(userId);
                pq.addString(query);
                pq.executeUpdate();
            }
        }
    }

    /**
     * Selects user queries with reverse sort by usage time.
     * @param userId user ID.
     * @return
     * @throws SQLException
     */
    public List<IdTitle> list(int userId) throws SQLException{
        List<IdTitle> result = new ArrayList<>();

        String query = SQL_SELECT + "id, data " + SQL_FROM + QUERY_HISTORY_TABLE
                + SQL_WHERE + "user_id=?"
                + SQL_ORDER_BY + "last_dt" + SQL_DESC;

        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(userId);
            
            var rs = pq.executeQuery();
            while (rs.next())
                result.add(new IdTitle(rs.getInt("id"), rs.getString("data")));
        }

        return result;
    }

    /**
     * Gets user history query.
     * @param userId user ID.
     * @param id query entity ID.
     * @return
     * @throws SQLException
     */
    public String get(int userId, int id) throws SQLException {
        String res = "";

        try (var ps = new PreparedQuery(con, SQL_SELECT + "data" +  SQL_FROM + QUERY_HISTORY_TABLE + SQL_WHERE + "user_id=? AND id=?")) {
            ps.addInt(userId);
            ps.addInt(id);

            var rs = ps.executeQuery();
            if (rs.next())
                res = rs.getString("data");
        }

        return res;
    }

    /**
     * Deletes user history query.
     * @param userId
     * @param id
     * @throws SQLException
     */
    public void delete(int userId, int id) throws SQLException {
        try (var ps = new PreparedQuery(con, SQL_DELETE + QUERY_HISTORY_TABLE + SQL_WHERE + "user_id=? AND id=?")) {
            ps.addInt(userId);
            ps.addInt(id);

            ps.executeUpdate();
        }
    }
}
