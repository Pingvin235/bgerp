package ru.bgcrm.dao.user;

import static ru.bgcrm.dao.user.Tables.TABLE_PERMSET_PERMISSION;
import static ru.bgcrm.dao.user.Tables.TABLE_USER_PERMSET_TITLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.util.Utils;

public class UserPermsetDAO extends UserDAO {
    public UserPermsetDAO(Connection con) {
        super(con);
    }

    public void searchPermset(Pageable<Permset> searchResult) {
        searchPermset(searchResult, null);
    }

    public void searchPermset(Pageable<Permset> searchResult, String filterLike) {
        Page page = searchResult.getPage();
        try {
            List<Permset> list = searchResult.getList();

            PreparedQuery pq = new PreparedQuery(con);
            pq.addQuery(SQL_SELECT_COUNT_ROWS + " p.*, pp.permset_id FROM " + TABLE_USER_PERMSET_TITLE + " AS p ");
            pq.addQuery("LEFT JOIN " + TABLE_PERMSET_PERMISSION + " AS pp ON p.id=pp.permset_id ");
            if (Utils.notBlankString(filterLike)) {
                pq.addQuery("AND pp.config LIKE ? ");
                pq.addString(filterLike);
            }

            pq.addQuery(" GROUP BY p.id ");

            if (Utils.notBlankString(filterLike)) {
                pq.addQuery("HAVING title LIKE ? OR comment LIKE ? OR config LIKE ? OR pp.permset_id>0");
                pq.addString(filterLike);
                pq.addString(filterLike);
                pq.addString(filterLike);
            }
            pq.addQuery(" ORDER BY p.title");
            pq.addQuery(getPageLimit(page));

            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                list.add(getFromRS(rs));
            }

            if (page != null) {
                page.setRecordCount(foundRows(pq.getPrepared()));
            }
            pq.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<Permset> getPermsetList() {
        List<Permset> result = new ArrayList<>();

        try {
            String query = "SELECT * FROM " + TABLE_USER_PERMSET_TITLE + " ORDER BY title";
            PreparedStatement ps = con.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getFromRS(rs));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public Permset getPermsetById(int id) {
        Permset result = null;

        try {
            String query = "SELECT * FROM " + TABLE_USER_PERMSET_TITLE + " WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getFromRS(rs);
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public void deletePermset(int id) throws SQLException {
        deleteById(TABLE_USER_PERMSET_TITLE, id);
    }

    private Permset getFromRS(ResultSet rs) throws SQLException {
        Permset result = new Permset();

        result.setId(rs.getInt("id"));
        result.setTitle(rs.getString("title"));
        result.setRoles(rs.getString("roles"));
        result.setComment(rs.getString("comment"));
        result.setConfig(rs.getString("config"));

        return result;
    }

    public void updatePermset(Permset userGroup) {
        int index = 1;
        PreparedStatement ps;

        try {
            if (userGroup.getId() <= 0) {
                ps = con.prepareStatement(
                        "INSERT INTO " + TABLE_USER_PERMSET_TITLE + " (title, roles, comment) VALUES (?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(index++, userGroup.getTitle());
                ps.setString(index++, userGroup.getRoles());
                ps.setString(index++, userGroup.getComment());
                ps.executeUpdate();
                userGroup.setId(lastInsertId(ps));
            } else {
                ps = con.prepareStatement("UPDATE " + TABLE_USER_PERMSET_TITLE
                        + " SET title=?,  roles=?, comment=?, config=? WHERE id=?");
                ps.setString(index++, userGroup.getTitle());
                ps.setString(index++, userGroup.getRoles());
                ps.setString(index++, userGroup.getComment());
                ps.setString(index++, userGroup.getConfig());
                ps.setInt(index++, userGroup.getId());

                ps.executeUpdate();
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public Map<Integer, Map<String, ConfigMap>> getAllPermsets() throws SQLException {
        return getAllPermissions(Tables.TABLE_PERMSET_PERMISSION, "permset_id");
    }

    public void updatePermissions(Set<String> action, Set<String> config, int permsetId) throws SQLException {
        updatePermissions(action, config, TABLE_PERMSET_PERMISSION, "permset_id", permsetId);
    }

    public Map<String, ConfigMap> getPermissions(int permsetId) {
        try {
            String query = "SELECT * FROM " + TABLE_PERMSET_PERMISSION + " WHERE permset_id=?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, permsetId);

            Map<String, ConfigMap> perms = new HashMap<>();

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String action = rs.getString("action");
                String config = rs.getString("config");

                perms.put(action, new Preferences(config));
            }
            ps.close();

            return perms;
        } catch (SQLException e) {
            throw new BGException();
        }
    }

    public void replacePermissions(int fromPermsetId, int toPermsetId) {
        try {
            String query = "DELETE FROM " + TABLE_PERMSET_PERMISSION + " WHERE permset_id=?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, toPermsetId);
            ps.executeUpdate();
            ps.close();

            query = "INSERT INTO " + TABLE_PERMSET_PERMISSION + " (permset_id, action, config) "
                    + "SELECT ?, action, config FROM " + TABLE_PERMSET_PERMISSION + "WHERE permset_id=?";

            ps = con.prepareStatement(query);
            ps.setInt(1, toPermsetId);
            ps.setInt(2, fromPermsetId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }
}