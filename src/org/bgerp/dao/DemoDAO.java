package org.bgerp.dao;

import static org.bgerp.dao.Tables.TABLE_DEMO_ENTITY;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bgerp.model.DemoEntity;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.LikePattern;
import org.bgerp.util.sql.PreparedQuery;

import javassist.NotFoundException;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.util.Utils;

public class DemoDAO extends CommonDAO {
    public DemoDAO(Connection con) {
        super(con);
    }

    public DemoEntity getOrThrow(int id) throws SQLException, NotFoundException {
        String query = SQL_SELECT_ALL_FROM + TABLE_DEMO_ENTITY + SQL_WHERE + "id=?";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, id);

            var rs = ps.executeQuery();
            if (rs.next())
                return getFromRs(rs);
            else
                throw new NotFoundException("Not found entity with ID: " + id);
        }
    }

    public void update(DemoEntity entity) throws SQLException {
        int id = updateOrInsert(
            SQL_UPDATE + TABLE_DEMO_ENTITY + SQL_SET + "title=?, config=?" + SQL_WHERE + "id=?",
            SQL_INSERT_INTO + TABLE_DEMO_ENTITY + "(title, config, id)" + SQL_VALUES + "(?, ?, ?)",
            entity.getTitle(), entity.getConfig(), entity.getId());
        if (id > 0)
            entity.setId(id);
    }

    public void delete(int id) throws SQLException {
        deleteById(TABLE_DEMO_ENTITY, id);
    }

    public void search(Pageable<DemoEntity> result, String filter) throws SQLException {
        var page = result.getPage();

        try (var pq = new PreparedQuery(con)) {
            pq.addQuery(SQL_SELECT_COUNT_ROWS + "*" + SQL_FROM + TABLE_DEMO_ENTITY + SQL_WHERE + "1>0");
            if (Utils.notBlankString(filter)) {
                pq.addQuery(SQL_AND + "(title LIKE ? OR config LIKE ?)");
                String pattern = LikePattern.SUB.get(filter);
                pq.addString(pattern).addString(pattern);
            }
            pq.addQuery(SQL_ORDER_BY + "title");
            pq.addQuery(getPageLimit(page));

            var rs = pq.executeQuery();
            while (rs.next())
                result.add(getFromRs(rs));

            setRecordCount(result.getPage(), pq.getPrepared());
        }
    }

    private DemoEntity getFromRs(ResultSet rs) throws SQLException {
        var entity = new DemoEntity();
        entity.setId(rs.getInt("id"));
        entity.setTitle(rs.getString("title"));
        entity.setConfig(rs.getString("config"));
        return entity;
    }
}
