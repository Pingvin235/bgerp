package org.bgerp.plugin.svc.dba.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.plugin.svc.dba.model.TableStatus;

import ru.bgcrm.dao.CommonDAO;

public class DatabaseDAO extends CommonDAO {
    public DatabaseDAO(Connection con) {
        super(con);
    }

    /**
     * Full list of DB tables info.
     * @return list alphabetically sorted by table names.
     * @throws SQLException
     */
    public List<TableStatus> tables() throws SQLException {
        var result = new ArrayList<TableStatus>();

        String query = "SELECT * FROM information_schema.tables WHERE table_schema=DATABASE() ORDER BY table_name";
        try (var ps = con.prepareStatement(query)) {
            var rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getFromRs(rs));
            }
        }

        return result;
    }

    private TableStatus getFromRs(ResultSet rs) throws SQLException {
        var item = new TableStatus();

        item.setName(rs.getString("table_name"));
        item.setRows(rs.getLong("table_rows"));
        item.setDataLength(rs.getLong("data_length"));
        item.setIndexLength(rs.getLong("index_length"));
        item.setCreateTime(rs.getTimestamp("create_time"));
        item.setUpdateTime(rs.getTimestamp("update_time"));

        return item;
    }
}
