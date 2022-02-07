package ru.bgcrm.dao.user;

import static ru.bgcrm.dao.user.Tables.TABLE_USER;
import static ru.bgcrm.dao.user.Tables.TABLE_USER_GROUP;
import static ru.bgcrm.dao.user.Tables.TABLE_USER_PERMISSION;
import static ru.bgcrm.dao.user.Tables.TABLE_USER_PERMSET;
import static ru.bgcrm.dao.user.Tables.TABLE_USER_QUEUE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ParamValueSearchDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.PswdUtil;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class UserDAO extends CommonDAO {
    private static final String SELECT_PERMSETS_QUERY = "( SELECT GROUP_CONCAT(up.permset_id ORDER BY up.pos, up.permset_id SEPARATOR ',') FROM "
            + TABLE_USER_PERMSET + " AS up " + "WHERE up.user_id=user.id) AS permsets, ";

    private static final String SELECT_GROUPS_QUERY = "( SELECT GROUP_CONCAT(ug.group_id SEPARATOR ',') FROM "
            + TABLE_USER_GROUP + " AS ug " + "WHERE ug.user_id=user.id) AS groups ";

    public UserDAO(Connection con) {
        super(con);
    }

    public void searchUser(SearchResult<User> searchResult, String filterLike, Set<Integer> groupFilter,
            Set<Integer> groupSelectFilter, Date date, Set<Integer> permsetFilter, int statusFilter)
            throws SQLException {
        Page page = searchResult.getPage();
        PreparedQuery psDelay = new PreparedQuery(con);

        psDelay.addQuery("SELECT SQL_CALC_FOUND_ROWS DISTINCT user.*, ");
        psDelay.addQuery(SELECT_PERMSETS_QUERY);

        Date prevDay = date != null ? TimeUtils.getPrevDay(date) : null;

        if (date == null) {
            psDelay.addQuery(SELECT_GROUPS_QUERY);
        } else {
            psDelay.addQuery("( SELECT GROUP_CONCAT(ug.group_id SEPARATOR ',') FROM " + TABLE_USER_GROUP + " AS ug "
                    + "WHERE ug.user_id=user.id AND (ug.date_from IS NULL OR ug.date_from<=?) AND (ug.date_to >? OR ug.date_to IS NULL) ) AS `groups` ");

            psDelay.addTimestamp(TimeUtils.convertDateToTimestamp(date));
            psDelay.addTimestamp(TimeUtils.convertDateToTimestamp(prevDay));
        }

        psDelay.addQuery(" FROM " + TABLE_USER + " AS user ");

        if (groupFilter != null && groupFilter.size() > 0) {
            psDelay.addQuery(SQL_INNER_JOIN);
            psDelay.addQuery(
                    TABLE_USER_GROUP + " AS user_group ON user.id=user_group.user_id AND user_group.group_id IN(");
            psDelay.addQuery(Utils.toString(groupFilter));
            psDelay.addQuery(")");

            if (date != null) {
                psDelay.addQuery(
                        " AND (user_group.date_from IS NULL OR user_group.date_from<=?) AND (user_group.date_to>? OR user_group.date_to IS NULL) ");

                psDelay.addTimestamp(TimeUtils.convertDateToTimestamp(date));
                psDelay.addTimestamp(TimeUtils.convertDateToTimestamp(prevDay));
            }
        }

        if (groupSelectFilter != null && groupSelectFilter.size() > 0) {
            psDelay.addQuery(SQL_INNER_JOIN);
            psDelay.addQuery(TABLE_USER_GROUP
                    + " AS user_select_group ON user.id=user_select_group.user_id AND user_select_group.group_id IN(");
            psDelay.addQuery(Utils.toString(groupSelectFilter));
            psDelay.addQuery(")");

            if (date != null) {
                psDelay.addQuery(
                        " AND user_select_group.date_from <=? AND (user_select_group.date_to >? OR user_select_group.date_to IS NULL) ");

                psDelay.addTimestamp(TimeUtils.convertDateToTimestamp(date));
                psDelay.addTimestamp(TimeUtils.convertDateToTimestamp(prevDay));
            }
        }

        if (permsetFilter != null && permsetFilter.size() > 0) {
            psDelay.addQuery(SQL_INNER_JOIN);
            psDelay.addQuery(TABLE_USER_PERMSET
                    + " AS user_permset ON user.id=user_permset.user_id AND user_permset.permset_id IN(");
            psDelay.addQuery(Utils.toString(permsetFilter));
            psDelay.addQuery(")");
        }

        psDelay.addQuery(" WHERE 1=1 ");

        if (Utils.notBlankString(filterLike)) {
            psDelay.addQuery(" AND ( title LIKE ? OR login LIKE ? OR description LIKE ? ) ");
            psDelay.addString(filterLike);
            psDelay.addString(filterLike);
            psDelay.addString(filterLike);
        }

        if (statusFilter >= 0) {
            psDelay.addQuery(" AND status=? ");
            psDelay.addInt(statusFilter);
        }

        psDelay.addQuery(" ORDER BY title ");
        psDelay.addQuery(getPageLimit(page));

        ResultSet rs = psDelay.executeQuery();
        while (rs.next()) {
            searchResult.getList().add(getFromRS(rs, "", true, false));
        }

        if (page != null) {
            page.setRecordCount(getFoundRows(psDelay.getPrepared()));
        }
        psDelay.close();
    }

    /**
     * Выбирает пользователей по параметру типа E-Mail.
     * @param searchResult
     * @param emailParamIdList
     * @param email E-Mail, поиск идёт по точному совпадению и совпадению домена
     * @throws SQLException
     */
    public void searchUserListByEmail(SearchResult<ParameterSearchedObject<User>> searchResult,
            List<Integer> emailParamIdList, String email) throws BGException {
        new ParamValueSearchDAO(con).searchObjectListByEmail(TABLE_USER, rs -> getFromRS(rs, "c.", false, false), searchResult,
                emailParamIdList, email);
    }

    /* TODO: Реализовать по аналогии, объединяя с CustomerDAO:
     * searchUserListByText
     * searchUserListByAddress
     * searchUserListByPhone
     */

    /**
     * Complete user list with passwords.
     * @return
     * @throws SQLException
     */
    public List<User> getUserList() throws SQLException {
        return getUserList(null, null, true);
    }

    public List<User> getUserList(Set<Integer> groupIds) throws SQLException {
        return getUserList(groupIds, null, false);
    }

    public List<User> getUserList(Set<Integer> groupIds, String userTitleMask) throws SQLException {
        return getUserList(groupIds, userTitleMask, false );
    }

    public List<User> getUserList(Set<Integer> groupIds, String userTitleMask, boolean loadPassword) throws SQLException {
        List<User> result = new ArrayList<User>();

        StringBuilder query = new StringBuilder().append("SELECT DISTINCT user.* FROM user ");
        if (groupIds != null && groupIds.size() > 0) {
            query.append(" INNER JOIN " + TABLE_USER_GROUP
                    + " ON user.id=user_group.user_id AND user_group.group_id IN (");
            query.append(Utils.toString(groupIds));
            query.append(")");
        }

        if (userTitleMask != null) {
            query.append(" WHERE title like '%" + userTitleMask + "%'");
        }
        query.append(" ORDER BY title");
        PreparedStatement ps = con.prepareStatement(query.toString());

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(getFromRS(rs, "", false, loadPassword));
        }
        ps.close();

        return result;
    }

    public List<User> getUserList(Set<Integer> groupFilter, Date dateGroupFrom, Date dateGroupTo) throws SQLException {
        List<User> result = new ArrayList<User>();

        PreparedQuery psDelay = new PreparedQuery(con);

        psDelay.addQuery("SELECT DISTINCT user.* ");
        psDelay.addQuery(" FROM " + TABLE_USER + " AS user ");

        if (CollectionUtils.isNotEmpty(groupFilter)) {
            psDelay.addQuery(SQL_INNER_JOIN);
            psDelay.addQuery(
                    TABLE_USER_GROUP + " AS user_group ON user.id=user_group.user_id AND user_group.group_id IN(");
            psDelay.addQuery(Utils.toString(groupFilter));
            psDelay.addQuery(")");

            if (dateGroupFrom != null) {
                psDelay.addQuery(" AND (user_group.date_to IS NULL OR ?<=user_group.date_to)");
                psDelay.addDate(TimeUtils.convertDateToSqlDate(dateGroupFrom));
            }
            if (dateGroupTo != null) {
                psDelay.addQuery(" AND (user_group.date_from IS NULL OR user_group.date_from<=?)");
                psDelay.addDate(TimeUtils.convertDateToSqlDate(dateGroupTo));
            }
        }

        psDelay.addQuery(" ORDER BY title ");

        ResultSet rs = psDelay.executeQuery();
        while (rs.next()) {
            result.add(getFromRS(rs));
        }
        psDelay.close();

        return result;
    }

    public Map<Integer, List<UserGroup>> getAllUserGroups() throws SQLException {
        Map<Integer, List<UserGroup>> result = new HashMap<Integer, List<UserGroup>>();

        String query = "SELECT * FROM " + TABLE_USER_GROUP;
        PreparedStatement ps = con.prepareStatement(query);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int userId = rs.getInt("user_id");
            UserGroup userGroup = getUserGroupFromRs(rs);

            if (result.get(userId) != null) {
                result.get(userId).add(userGroup);
            } else {
                List<UserGroup> groupList = new ArrayList<UserGroup>();

                groupList.add(userGroup);
                result.put(userId, groupList);
            }

        }
        ps.close();

        return result;
    }

    public Set<Integer> getUserGroupIds(int userId) throws SQLException {
        return getIds(TABLE_USER_GROUP, "user_id", "group_id", userId);
    }

    public Map<Integer, Set<Integer>> getAllUserGroupIds() throws BGException {
        return getGroupedIds(TABLE_USER_GROUP, "user_id", "group_id");
    }

    public List<Integer> getUserPermsetIds(int userId) throws SQLException {
        return getIds(TABLE_USER_PERMSET, "user_id", "permset_id", "pos", userId);
    }

    public Map<Integer, List<Integer>> getAllUserPermsetIds() throws BGException {
        return getGroupedIds(TABLE_USER_PERMSET, "user_id", "permset_id", "pos");
    }

    public Set<Integer> getUserQueueIds(int userId) throws SQLException {
        return getIds(TABLE_USER_QUEUE, "user_id", "queue_id", userId);
    }

    public Map<Integer, Set<Integer>> getAllUserQueueIds() throws BGException {
        return getGroupedIds(TABLE_USER_QUEUE, "user_id", "queue_id");
    }

    private User getFromRS(ResultSet rs) throws SQLException {
        return getFromRS(rs, "", false, false);
    }

    /**
     * Retrieving user data from query result set.
     *
     * @param rs result set. Don't forget to close rs!
     * @param prefix a prefix for a table column
     * @param loadGroupAndPermsets is need to load groups and permsets. true - need to load, false - doesn't need.
     * @param loadPassword is need to load password from database. true - need, false - doesn't need.
     * @return user entity
     */
    private User getFromRS(ResultSet rs, String prefix, boolean loadGroupAndPermsets, boolean loadPassword) throws SQLException {
        User user = new User();

        user.setId(rs.getInt(prefix + "id"));
        user.setTitle(rs.getString(prefix + "title"));
        user.setStatus(rs.getInt(prefix + "status"));
        user.setConfig(rs.getString(prefix + "config"));
        user.setPersonalization(prefix + rs.getString("personalization"));
        user.setDescription(rs.getString(prefix + "description"));
        user.setLogin(rs.getString(prefix + "login"));
        if (loadPassword) {
            user.setPassword(rs.getString(prefix + "pswd"));
        }
        if (loadGroupAndPermsets) {
            user.setPermsetIds(Utils.toIntegerList(rs.getString(prefix + "permsets")));
            user.setGroupIds(Utils.toIntegerSet(rs.getString(prefix + "groups")));
        }

        return user;
    }

    public void updateUser(int userId, String title, String login, String pswd, String description)
            throws SQLException {
        int index = 1;

        var ps = con.prepareStatement("UPDATE user SET title=?, login=?, description=? WHERE id=?");
        ps.setString(index++, title);
        ps.setString(index++, login);
        ps.setString(index++, description);
        ps.setInt(index++, userId);
        ps.executeUpdate();
        ps.close();

        updateUserPassword(userId, pswd);
    }

    public void updateUser(User user) throws SQLException {
        boolean newUser = false;
        int index = 1;
        PreparedStatement ps;

        if (user.getId() <= 0) {
            newUser = true;
            String query = "INSERT INTO " + TABLE_USER + " SET title=?, login=?, description=?, config=?, date_created=?, status=?";
            ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, user.getTitle());
            ps.setString(index++, user.getLogin());
            ps.setString(index++, user.getDescription());
            ps.setString(index++, user.getConfig());
            ps.setDate(index++, TimeUtils.convertDateToSqlDate(new Date()));
            ps.setInt(index++, user.getStatus());
            ps.executeUpdate();
            user.setId(lastInsertId(ps));
        } else {
            ps = con.prepareStatement(
                    "UPDATE " + TABLE_USER + " SET title=?, login=?, status=?, description=?, config=? WHERE id=?");
            ps.setString(index++, user.getTitle());
            ps.setString(index++, user.getLogin());
            ps.setInt(index++, user.getStatus());
            ps.setString(index++, user.getDescription());
            ps.setString(index++, user.getConfig());
            ps.setInt(index++, user.getId());
            ps.executeUpdate();
        }
        ps.close();

        updateUserPassword(user.getId(), user.getPassword());

        if (user.getPermsetIds() != null) {
            updateIds(TABLE_USER_PERMSET, "user_id", "permset_id", "pos", user.getId(), user.getPermsetIds());
        }
        if (user.getQueueIds() != null) {
            updateIds(TABLE_USER_QUEUE, "user_id", "queue_id", user.getId(), user.getQueueIds());
        }

        if (newUser && user.getGroupIds() != null) {
            updateUserGroups(user);
        }
    }

    private void updateUserPassword(int userId, String pswd) throws SQLException {
        if (PswdUtil.EMPTY_PASSWORD.equals(pswd))
            return;

        try (var pq = new PreparedQuery(con, SQL_UPDATE + TABLE_USER + SQL_SET + "pswd=?" + SQL_WHERE + "id=?")) {
            pq.addString(pswd).addInt(userId).executeUpdate();
        }
    }

    public User getUserByLogin(String name) throws BGException {
        User result = null;

        try {
            String query = "SELECT * FROM " + TABLE_USER + " WHERE login=? AND status=0";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getFromRS(rs);
            }

            if (result != null) {
                result.setGroupIds(getUserGroupIds(result.getId()));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    /**
     * Retrieves user by ID.
     * @param id unique ID.
     * @return found user or {@code null}.
     * @throws SQLException
     */
    public User getUser(int id) throws SQLException {
        User result = null;

        String query = "SELECT * FROM " + TABLE_USER + " WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            result = getFromRS(rs);
        }

        if (result != null) {
            result.setGroupIds(getUserGroupIds(id));
            result.setPermsetIds(getUserPermsetIds(id));
            result.setQueueIds(getUserQueueIds(id));
        }
        ps.close();

        return result;
    }

    public void deleteUser(int id) throws BGException {
        deleteById(TABLE_USER, id);
    }

    protected String getPageLimit(Page page) {
        StringBuilder sql = new StringBuilder();

        // ненулевой размер pageSize устанавливается в Response#addSearchResult
        if (page != null && page.getPageSize() > 0) {
            sql.append(" LIMIT ");
            sql.append(page.getPageFirstRecordNumber());
            sql.append(", ");
            sql.append(page.getPageSize());
        }

        return sql.toString();
    }

    public Map<Integer, Map<String, ParameterMap>> getAllPermissions(String tableName, String selectColumn)
            throws BGException {
        Map<Integer, Map<String, ParameterMap>> userPerm = new HashMap<Integer, Map<String, ParameterMap>>();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT " + selectColumn + ",action,config FROM " + tableName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt(selectColumn);
                String action = rs.getString("action");
                String config = rs.getString("config");

                Map<String, ParameterMap> map = userPerm.get(userId);
                if (map == null) {
                    userPerm.put(userId, map = new HashMap<String, ParameterMap>());
                }

                map.put(action, new Preferences(config));
                //userPerm.get( userId ).put( action, config );

            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return userPerm;
    }

    public Map<Integer, Map<String, ParameterMap>> getAllUserPerm() throws BGException {
        return getAllPermissions(Tables.TABLE_USER_PERMISSION, "user_id");
    }

    public void updatePermissions(Set<String> action, Set<String> config, int userId) throws SQLException {
        updatePermissions(action, config, TABLE_USER_PERMISSION, "user_id", userId);
    }

    protected void updatePermissions(Set<String> action, Set<String> config, String table, String column, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement("DELETE FROM " + table + " WHERE " + column + "=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();

        for (String newAction : action) {
            String newConfig = "";
            for (String c : config) {
                if (c.startsWith(newAction + "#")) {
                    newConfig = StringUtils.substringAfter(c, newAction + "#");
                    break;
                }
            }

            if (Utils.notEmptyString(newAction)) {
                ps = con.prepareStatement("INSERT INTO " + table + " (" + column + ", action, config) "
                    + "VALUES ( ? , ? , ? )");
                ps.setInt(1, id);
                ps.setString(2, newAction);
                ps.setString(3, newConfig);
                ps.executeUpdate();
                ps.close();
            }
        }
    }

    public Map<String, ParameterMap> getPermissions(int userId) throws BGException {
        try {
            PreparedStatement ps = null;

            ps = con.prepareStatement("SELECT * FROM " + TABLE_USER_PERMISSION + " WHERE user_id=?");
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            Map<String, ParameterMap> perms = new HashMap<String, ParameterMap>();

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

    /**
     * Обновить персональныую конфигурацию, только если она отличается от configBefore.
     * @param configBefore null, если нужно безусловное сохранение.
     * @param user
     * @throws BGException
     */
    public void updatePersonalization(String configBefore, User user) throws BGException {
        if (user.getPersonalizationMap().getDataString().equals(configBefore)) {
            return;
        }

        log.debug("Updating personalization %s", user.getId());

        try {
            PreparedStatement ps = con.prepareStatement("UPDATE " + TABLE_USER + "SET personalization=? WHERE id=?");
            ps.setString(1, user.getPersonalizationMap().getDataString());
            ps.setInt(2, user.getId());
            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            throw new BGException();
        }
    }

    public void updatePersonalization(User user, ParameterMap newProps) throws BGException {
        Preferences persMap = user.getPersonalizationMap();
        String configBefore = persMap.getDataString();

        for (Map.Entry<String, String> me : newProps.entrySet())
            persMap.put(me.getKey(), me.getValue());

        updatePersonalization(configBefore, user);
    }

    public void addUserGroup(int userId, UserGroup group) throws SQLException {
        String query = "INSERT INTO " + TABLE_USER_GROUP + " (user_id, group_id, date_from, date_to) "
                + "VALUES (?, ?, ?, ?) ";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, group.getGroupId());
        ps.setTimestamp(3, TimeUtils.convertDateToTimestamp(group.getDateFrom()));
        ps.setTimestamp(4, TimeUtils.convertDateToTimestamp(group.getDateTo()));

        ps.executeUpdate();
        ps.close();
    }

    public void removeUserGroup(int userId, int groupId, Date dateFrom, Date dateTo) throws SQLException {
        String query = SQL_DELETE + TABLE_USER_GROUP + SQL_WHERE + " user_id=" + userId
                + (groupId == -1 ? "" : " AND group_id=" + groupId);
        query += dateFrom != null ? " AND date_from = ? " : " AND date_from IS NULL ";
        query += dateTo != null ? " AND date_to = ? " : " AND date_to IS NULL ";

        int index = 1;
        PreparedStatement ps = con.prepareStatement(query);
        if (dateFrom != null)
            ps.setDate(index++, TimeUtils.convertDateToSqlDate(dateFrom));
        if (dateTo != null)
            ps.setDate(index++, TimeUtils.convertDateToSqlDate(dateTo));
        ps.executeUpdate();
        ps.close();
    }

    public List<UserGroup> getUserGroupList(int userId, Date date) throws BGException {
        List<UserGroup> result = new ArrayList<UserGroup>();

        try {
            PreparedQuery pq = new PreparedQuery(con);
            pq.addQuery("SELECT * FROM " + TABLE_USER_GROUP + " WHERE user_id=? ");
            pq.addInt(userId);

            if (date != null) {
                pq.addQuery("AND (date_from IS NULL OR date_from<=?) AND (date_to IS NULL OR ?<=date_to) ");
                pq.addDate(date);
                pq.addDate(date);
            }

            pq.addQuery("ORDER BY date_from");

            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                result.add(getUserGroupFromRs(rs));
            }
            pq.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    /**
     * Sets user groups to values from {@link User#getGroupIds()}, opened from current date.
     * All the existing groups are replaced.
     * @param user
     * @throws SQLException
     */
    public void updateUserGroups(User user) throws SQLException {
        this.removeUserGroup(user.getId(), -1, null, null);

        for (Integer groupId : user.getGroupIds()) {
            UserGroup group = new UserGroup();

            group.setGroupId(groupId);
            group.setDateFrom(new Date());

            this.addUserGroup(user.getId(), group);
        }
    }

    private UserGroup getUserGroupFromRs(ResultSet rs) throws SQLException {
        return new UserGroup(rs.getInt("group_id"), rs.getTimestamp("date_from"), rs.getTimestamp("date_to"));
    }

    public void closeUserGroupPeriod(int userId, int groupId, Date date, Date dateFrom, Date dateTo)
            throws SQLException {
        String query = SQL_UPDATE + TABLE_USER_GROUP + " SET date_to=? " + SQL_WHERE + " user_id=? AND group_id=?";
        query += dateFrom != null ? " AND date_from = ? " : " AND date_from IS NULL ";
        query += dateTo != null ? " AND date_to = ? " : " AND date_to IS NULL ";

        int index = 1;
        PreparedStatement ps = con.prepareStatement(query);
        ps.setDate(index++, TimeUtils.convertDateToSqlDate(date));
        ps.setInt(index++, userId);
        ps.setInt(index++, groupId);
        if (dateFrom != null)
            ps.setDate(index++, TimeUtils.convertDateToSqlDate(dateFrom));
        if (dateTo != null)
            ps.setDate(index++, TimeUtils.convertDateToSqlDate(dateTo));

        ps.executeUpdate();
        ps.close();
    }
}
