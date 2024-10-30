package ru.bgcrm.dao.user;

import static ru.bgcrm.dao.user.Tables.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.LikePattern;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class UserGroupDAO extends CommonDAO {
    public UserGroupDAO(Connection con) {
        super(con);
    }

    public void searchGroup(Pageable<Group> searchResult, int parentId) {
        searchGroup(searchResult, parentId, 0, "");
    }

    public void searchGroup(Pageable<Group> searchResult, int parentId, int archive) {
        searchGroup(searchResult, parentId, 0, "");
    }

    public void searchGroup(Pageable<Group> searchResult, int parentId, int archive, String filter) {
        Page page = searchResult.getPage();
        try {
            List<Group> list = searchResult.getList();

            PreparedQuery pq = new PreparedQuery(con);

            pq.addQuery(SQL_SELECT_COUNT_ROWS + "g.*, " + "( SELECT GROUP_CONCAT(gp.permset_id SEPARATOR ',') FROM " + TABLE_USER_GROUP_PERMSET
                    + " AS gp WHERE gp.group_id=g.id ORDER BY pos) AS permsets, " + "( SELECT GROUP_CONCAT(gq.queue_id SEPARATOR ',') FROM "
                    + TABLE_USER_GROUP_QUEUE + " AS gq WHERE gq.group_id=g.id) AS queues " + " FROM " + TABLE_USER_GROUP_TITLE + " AS g "
                    + " WHERE ");

            if (Utils.notBlankString(filter)) {
                pq.addQuery("(title LIKE ? OR config LIKE ?)");
                pq.addString(LikePattern.SUB.get(filter));
                pq.addString(LikePattern.SUB.get(filter));
            } else {
                pq.addQuery("parent_id=?");
                pq.addInt(parentId);
            }

            pq.addQuery(" ORDER BY title" + getPageLimit(page));

            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                list.add(getFromRs(rs, true));
            }

            if (page != null) {
                page.setRecordCount(foundRows(pq.getPrepared()));
            }
            pq.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<Group> getGroupList() {
        List<Group> result = new ArrayList<>();

        try {
            String query = "SELECT * FROM " + TABLE_USER_GROUP_TITLE + " ORDER BY title";
            PreparedStatement ps = con.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getFromRs(rs, false));
            }
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }

        return result;
    }

    public Group getGroupById(int id) {
        Group result = null;

        try {
            String query = "SELECT * FROM " + TABLE_USER_GROUP_TITLE + " WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getFromRs(rs, false);
                result.setQueueIds(getGroupQueueIds(id));
                result.setPermsetIds(getGroupPermsetIds(id));
            }
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }

        return result;
    }

    public void updateGroup(Group group) {
        try {
            PreparedStatement ps = null;

            if (group.getId() <= 0) {
                ps = con.prepareStatement(
                        "INSERT INTO " + TABLE_USER_GROUP_TITLE + " (title, description, parent_id, archive, config) VALUES (?,?,?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
            } else {
                ps = con.prepareStatement(
                        "UPDATE " + TABLE_USER_GROUP_TITLE + " SET title=?, description=?, parent_id=?, archive=?, config=? WHERE id=?");
                ps.setInt(6, group.getId());
            }

            ps.setString(1, group.getTitle());
            ps.setString(2, group.getComment());
            ps.setInt(3, group.getParentId());
            ps.setInt(4, group.getArchive());
            ps.setString(5, group.getConfig());
            ps.executeUpdate();

            if (group.getId() <= 0) {
                group.setId(SQLUtils.lastInsertId(ps));
            }

            updateIds(TABLE_USER_GROUP_QUEUE, "group_id", "queue_id", group.getId(), group.getQueueIds());
            updateIds(TABLE_USER_GROUP_PERMSET, "group_id", "permset_id", "pos", group.getId(), group.getPermsetIds());

            setChildCount(group.getId(), 0);

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void deleteGroup(int id) throws SQLException {
        setChildCount(id, -1);
        deleteById(TABLE_USER_GROUP_TITLE, id);
    }

    public Set<Integer> getGroupQueueIds(int groupId) throws SQLException {
        return getIds(TABLE_USER_GROUP_QUEUE, "group_id", "queue_id", groupId);
    }

    public List<Integer> getGroupPermsetIds(int groupId) throws SQLException {
        return getIds(TABLE_USER_GROUP_PERMSET, "group_id", "permset_id", "pos", groupId);
    }

    public Map<Integer, List<Integer>> getAllGroupPermsetIds() throws SQLException {
        return getGroupedIds(TABLE_USER_GROUP_PERMSET, "group_id", "permset_id", "pos");
    }

    public Map<Integer, Set<Integer>> getAllGroupQueueIds() throws SQLException {
        return getGroupedIds(TABLE_USER_GROUP_QUEUE, "group_id", "queue_id");
    }

    private Group getFromRs(ResultSet rs, boolean loadQueuesAndPermsets) throws SQLException, BGException {
        Group result = new Group();

        result.setId(rs.getInt("id"));
        result.setTitle(rs.getString("title"));
        result.setComment(rs.getString("description"));
        result.setParentId(rs.getInt("parent_id"));
        result.setArchive(rs.getInt("archive"));
        result.setChildCount(rs.getInt("child_count"));
        result.setConfig(rs.getString("config"));

        if (loadQueuesAndPermsets) {
            result.setPermsetIds(Utils.toIntegerList(rs.getString("permsets")));
            result.setQueueIds(Utils.toIntegerSet(rs.getString("queues")));
        }

        return result;
    }

    /**
     * true если можно добавить скрипт с таким именем в данный каталог
     * @param parentId
     * @param name
     * @return
     */
    public boolean checkGroup(int id, int parentId, String title) throws SQLException {
        boolean result = false;

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("COUNT(*)");
        query.append(SQL_FROM);
        query.append(TABLE_USER_GROUP_TITLE);
        query.append(SQL_WHERE);
        query.append("id !=? AND parent_id=? AND title=?");
        PreparedStatement ps = con.prepareStatement(query.toString());
        int index = 1;
        ps.setInt(index++, id);
        ps.setInt(index++, parentId);
        ps.setString(index++, title);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = rs.getInt(1) == 0;
        }
        ps.close();

        return result;
    }

    private void setChildCount(int chilsId, int countCorrect) throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps = null;

        ps = con.prepareStatement("select count(*), t1.parent_id from " + TABLE_USER_GROUP_TITLE + " as t1 join " + TABLE_USER_GROUP_TITLE
                + " as t2 where t2.id = ? AND t1.parent_id = t2.parent_id");
        ps.setInt(1, chilsId);
        rs = ps.executeQuery();

        int count = 0;
        int parentId = 0;

        if (rs.next()) {
            count = rs.getInt(1);
            parentId = rs.getInt(2);
        }

        ps.close();

        int index = 1;
        count += countCorrect;
        PreparedStatement psUpdate = con.prepareStatement("UPDATE " + TABLE_USER_GROUP_TITLE + " SET child_count=? WHERE id=? ");

        psUpdate.setInt(index++, count);
        psUpdate.setInt(index++, parentId);
        psUpdate.executeUpdate();
        psUpdate.close();
    }
}
