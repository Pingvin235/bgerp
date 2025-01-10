package org.bgerp.dao.param;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.exception.BGException;
import org.bgerp.model.Pageable;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.tree.IdStringTitleTreeItem;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.util.Utils;

public class ParamDAO extends CommonDAO {
    public ParamDAO(Connection con) {
        super(con);
    }

    public Parameter getParameter(int id) throws SQLException {
        Parameter parameter = null;

        try (var ps = con.prepareStatement(SQL_SELECT_ALL_FROM + "param_pref" + SQL_WHERE + "id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                parameter = getParameterFromRs(rs);
                String type = parameter.getType();

                if (Parameter.TYPE_LIST.equals(type))
                    parameter.setValuesConfig(getListParamValuesConfig(id));
                else if (Parameter.TYPE_LISTCOUNT.equals(parameter.getType()))
                    parameter.setValuesConfig(getListCountParamValuesConfig(id));
                else if (Parameter.TYPE_TREE.equals(parameter.getType()))
                    parameter.setValuesConfig(getTreeParamValuesConfig(id, Tables.TABLE_PARAM_TREE_VALUE));
                else if (Parameter.TYPE_TREECOUNT.equals(parameter.getType()))
                    parameter.setValuesConfig(getTreeParamValuesConfig(id, Tables.TABLE_PARAM_TREECOUNT_VALUE));

            }
        }

        return parameter;
    }

    private String getTreeParamValuesConfig(int paramId, String tableName) throws SQLException {
        IdStringTitleTreeItem root = getTreeParamRootNode(paramId, tableName);
        return getTreeConfig(root, "");
    }

    private String getTreeConfig(IdStringTitleTreeItem node, String prefix) {
        StringBuilder config = new StringBuilder();

        if (Utils.notBlankString(node.getId())) {
            config.append(prefix + "=" + node.getTitle());
            config.append("\n");
        }

        List<IdStringTitleTreeItem> children = node.getChildren();
        for (IdStringTitleTreeItem child : children) {
            String curPrefix = prefix;

            if (Utils.notBlankString(child.getParentId())) {
                curPrefix += String.valueOf(child.getId()).substring(String.valueOf(child.getParentId()).length());
            } else {
                curPrefix += String.valueOf(child.getId());
            }

            config.append(getTreeConfig(child, curPrefix));
        }

        return config.toString();
    }

    private String getListCountParamValuesConfig(int paramId) throws SQLException {
        StringBuilder query = new StringBuilder();
        ResultSet rs = null;
        PreparedStatement ps = null;
        query.append("SELECT id, title FROM ");
        query.append(Tables.TABLE_PARAM_LISTCOUNT_VALUE);
        query.append(" WHERE param_id=? ORDER BY id");
        ps = con.prepareStatement(query.toString());
        ps.setInt(1, paramId);
        rs = ps.executeQuery();

        StringBuilder result = new StringBuilder(100);
        while (rs.next()) {
            result.append(rs.getInt(1));
            result.append("=");
            result.append(rs.getString(2));
            if (!rs.isLast()) {
                result.append("\n");
            }
        }

        ps.close();

        return result.toString();
    }

    public Map<Integer, IdStringTitleTreeItem> getTreeParamRootNodes() throws SQLException {
        Map<Integer, IdStringTitleTreeItem> result = new HashMap<>(2000);

        loadTreeParamRootNodes(result, Tables.TABLE_PARAM_TREE_VALUE);
        loadTreeParamRootNodes(result, Tables.TABLE_PARAM_TREECOUNT_VALUE);

        return Collections.unmodifiableMap(result);
    }

    private void loadTreeParamRootNodes(Map<Integer, IdStringTitleTreeItem> result, String tableName) throws SQLException {
        StringBuilder query = new StringBuilder(200);
        query.append(SQL_SELECT + "DISTINCT(param_id)" + SQL_FROM);
        query.append(tableName);
        query.append(SQL_ORDER_BY + "id");

        try (var ps = con.prepareStatement(query.toString())) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Integer paramId = rs.getInt("param_id");
                result.put(paramId, getTreeParamRootNode(paramId, tableName));
            }
        }
    }

    private IdStringTitleTreeItem getTreeParamRootNode(int paramId, String tableName) throws SQLException {
        IdStringTitleTreeItem root = new IdStringTitleTreeItem("", "", "");

        List<IdStringTitleTreeItem> items = new ArrayList<>();

        try (var ps = con.prepareStatement(SQL_SELECT + "id, parent_id, title" + SQL_FROM + tableName + SQL_WHERE + "param_id=?" + SQL_ORDER_BY + "id")) {
            ps.setInt(1, paramId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                items.add(new IdStringTitleTreeItem(rs.getString("id"), rs.getString("title"), rs.getString("parent_id")));
        }

        Collections.sort(items, IdStringTitleTreeItem.COMPARATOR);

        items.forEach((item) -> {
            IdStringTitleTreeItem child = root.getChild(item.getParentId());
            if (child != null)
                child.addChild(item);
            else
                root.addChild(item);
        });

        return root;
    }

    public void updateParameter(Parameter parameter) throws SQLException {
        int index = 1;
        String query = null;
        PreparedStatement ps = null;

        if (parameter.getId() <= 0) {
            query = "INSERT INTO param_pref SET object=?, type=?, title=?, `order`=?, config=?, comment=?";
            ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, parameter.getObjectType());
            ps.setString(index++, parameter.getType());
            ps.setString(index++, parameter.getTitle());
            ps.setInt(index++, parameter.getOrder());
            ps.setString(index++, parameter.getConfig());
            ps.setString(index++, parameter.getComment());
            ps.executeUpdate();
            parameter.setId(lastInsertId(ps));
        } else {
            query = "UPDATE param_pref SET title=?, `order`=?, config=?, comment=? WHERE id=?";
            ps = con.prepareStatement(query);
            ps.setString(index++, parameter.getTitle());
            ps.setInt(index++, parameter.getOrder());
            ps.setString(index++, parameter.getConfig());
            ps.setString(index++, parameter.getComment());
            ps.setInt(index++, parameter.getId());
            ps.executeUpdate();
        }
        ps.close();

        if (Parameter.TYPE_LIST.equals(parameter.getType()))
            updateListValues(parameter, Tables.TABLE_PARAM_LIST_VALUE);
        else if (Parameter.TYPE_LISTCOUNT.equals(parameter.getType()))
            updateListValues(parameter, Tables.TABLE_PARAM_LISTCOUNT_VALUE);
        else if (Parameter.TYPE_TREE.equals(parameter.getType()))
            updateTreeValues(parameter, Tables.TABLE_PARAM_TREE_VALUE);
        else if (Parameter.TYPE_TREECOUNT.equals(parameter.getType()))
            updateTreeValues(parameter, Tables.TABLE_PARAM_TREECOUNT_VALUE);
    }

    private void updateListValues(Parameter parameter, String tableName) throws SQLException {
        try (var ps = con.prepareStatement(SQL_DELETE_FROM + tableName + SQL_WHERE + "param_id=?")) {
            ps.setInt(1, parameter.getId());
            ps.executeUpdate();
        }
        try (var ps = con.prepareStatement(SQL_INSERT_INTO + tableName + " (id, title, param_id) VALUES (?, ?, ?)")) {
            ps.setInt(3, parameter.getId());
            for (Map.Entry<Integer, String> me : convertListValuesConfigToMap(parameter.getValuesConfig()).entrySet()) {
                ps.setInt(1, me.getKey());
                ps.setString(2, me.getValue());
                ps.executeUpdate();
            }
        }
    }

    private void updateTreeValues(Parameter parameter, String tableName) throws SQLException {
        try (var ps = con.prepareStatement(SQL_DELETE_FROM + tableName + SQL_WHERE + "param_id=?")) {
            ps.setInt(1, parameter.getId());
            ps.executeUpdate();
        }
        try (var ps = con.prepareStatement(SQL_INSERT_INTO + tableName + " (id, parent_id, title, param_id) VALUES (?, ?, ?, ?)")) {
            ps.setInt(4, parameter.getId());
            for (var node : convertTreeValuesConfigToNodeList(parameter.getValuesConfig())) {
                ps.setString(1, node.getId());
                ps.setString(2, node.getParentId());
                ps.setString(3, node.getTitle());
                ps.executeUpdate();
            }
        }
    }

    private List<IdStringTitleTreeItem> convertTreeValuesConfigToNodeList(String valuesConfig) {
        List<IdStringTitleTreeItem> nodes = new ArrayList<>();
        if (valuesConfig == null) {
            return nodes;
        }

        for (String line : valuesConfig.split("\\n")) {
            String[] id_value = line.split("=");
            if (id_value.length < 2) {
                continue;
            }

            String id = id_value[0];
            String parentId = "";
            if (id_value[0].contains(".")) {
                int lastIndex = id_value[0].lastIndexOf(".");
                parentId = id_value[0].substring(0, lastIndex);
            }

            nodes.add(new IdStringTitleTreeItem(id, id_value[1], parentId));
        }

        return nodes;
    }

    public void deleteParameter(int id) {
        try {
            String query = "DELETE FROM " + Tables.TABLE_PARAM_PREF + " WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            query = "DELETE FROM " + Tables.TABLE_PARAM_LIST_VALUE + " WHERE param_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public Map<String, List<Parameter>> getParameterMapByObjectType() throws SQLException {
        Map<String, List<Parameter>> result = new HashMap<>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT param_pref.* FROM ");
        query.append(Tables.TABLE_PARAM_PREF);
        query.append("ORDER BY object, `order`");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Parameter param = getParameterFromRs(rs);

            List<Parameter> paramList = result.get(param.getObjectType());
            if (paramList == null) {
                paramList = new ArrayList<>();
                result.put(param.getObjectType(), paramList);
            }

            paramList.add(param);
        }
        ps.close();

        return result;
    }

    public Map<Integer, Set<Integer>> getParameterIdsByGroupIds() throws SQLException {
        Map<Integer, Set<Integer>> result = new HashMap<>();

        StringBuilder query = new StringBuilder(200);
        query.append("SELECT group_id, param_id FROM ");
        query.append(Tables.TABLE_PARAM_GROUP);

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int groupId = rs.getInt(1);

            Set<Integer> paramIds = result.get(groupId);
            if (paramIds == null) {
                paramIds = new HashSet<>();
                result.put(groupId, paramIds);
            }
            paramIds.add(rs.getInt(2));
        }
        ps.close();

        return result;
    }

    public Map<Integer, List<IdTitle>> getListParamValuesMap() throws SQLException {
        Map<Integer, List<IdTitle>> result = new LinkedHashMap<>();

        StringBuilder query = new StringBuilder(200);

        query.append("SELECT id, param_id, title FROM ");
        query.append(Tables.TABLE_PARAM_LIST_VALUE);
        query.append("UNION SELECT id, param_id, title FROM ");
        query.append(Tables.TABLE_PARAM_LISTCOUNT_VALUE);

        PreparedStatement ps = con.prepareStatement(query.toString());

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int paramId = rs.getInt(2);

            List<IdTitle> valueList = result.get(paramId);
            if (valueList == null) {
                result.put(paramId, valueList = new ArrayList<>());
            }

            valueList.add(new IdTitle(rs.getInt(1), rs.getString(3)));
        }
        ps.close();

        return result;
    }

    private String getListParamValuesConfig(int paramId) throws SQLException {
        StringBuilder query = new StringBuilder();
        ResultSet rs = null;
        PreparedStatement ps = null;
        query.append("SELECT id, title FROM ");
        query.append(Tables.TABLE_PARAM_LIST_VALUE);
        query.append(" WHERE param_id=?");
        ps = con.prepareStatement(query.toString());
        ps.setInt(1, paramId);
        rs = ps.executeQuery();

        StringBuilder result = new StringBuilder(100);
        while (rs.next()) {
            result.append(rs.getInt(1));
            result.append("=");
            result.append(rs.getString(2));
            if (!rs.isLast()) {
                result.append("\n");
            }
        }

        ps.close();

        return result.toString();
    }

    private Map<Integer, String> convertListValuesConfigToMap(String valuesConfig) {
        Map<Integer, String> listValuesMap = new LinkedHashMap<>();

        if (valuesConfig == null) {
            return listValuesMap;
        }

        for (String line : valuesConfig.split("\\n")) {
            String[] id_value = line.split("=");
            if (id_value.length < 2) {
                continue;
            }

            listValuesMap.put(Utils.parseInt(id_value[0]), id_value[1]);
        }

        return listValuesMap;
    }

    public void searchParameter(Pageable<Parameter> result, String objectType, String filter, int paramGroupId, Set<Integer> parameterIds)
            throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            pq.addQuery(SQL_SELECT_COUNT_ROWS + "param_pref.*" + SQL_FROM + Tables.TABLE_PARAM_PREF);

            if (Customer.OBJECT_TYPE.equals(objectType) && paramGroupId > 0) {
                pq.addQuery(SQL_INNER_JOIN + "param_group ON param_pref.id=param_group.param_id AND param_group.group_id=?");
                pq.addInt(paramGroupId);
            }

            pq.addQuery(SQL_WHERE + "object=?");
            pq.addString(objectType);

            if (Utils.notBlankString(filter)) {
                pq.addQuery(" AND (param_pref.id LIKE ? OR param_pref.title LIKE ? OR param_pref.comment LIKE ? OR param_pref.config LIKE ?)");

                pq.addString(filter).addString(filter).addString(filter).addString(filter);
            }

            if (parameterIds != null && !parameterIds.isEmpty())
                pq.addQuery(" AND param_pref.id IN ( ").addQuery(Utils.toString(parameterIds)).addQuery(" )");

            pq.addQuery(SQL_ORDER_BY + "`order`, title");

            Page page = result.getPage();

            pq.addQuery(getPageLimit(page));

            ResultSet rs = pq.executeQuery();
            while (rs.next())
                result.add(getParameterFromRs(rs));

            if (page != null)
                page.setRecordCount(foundRows(pq.getPrepared()));
        }
    }

    public List<Parameter> getParameterList(String objectType, int paramGroupId) throws SQLException {
        return getParameterList(objectType, paramGroupId, null);
    }

    public List<Parameter> getParameterList(String objectType, int paramGroupId, Set<Integer> parameterIdList) throws SQLException {
        List<Parameter> result = new ArrayList<>();

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();

        query.append("SELECT param_pref.* FROM ");
        query.append(Tables.TABLE_PARAM_PREF);
        if (objectType != null && Customer.OBJECT_TYPE.equals(objectType) && paramGroupId > 0) {
            query.append(" LEFT JOIN param_group ON param_pref.id=param_group.param_id");
        }
        query.append(" WHERE object=?");
        if (parameterIdList != null && parameterIdList.size() > 0) {
            StringBuilder pids = new StringBuilder();
            for (Integer pid : parameterIdList) {
                if (pids.length() > 0) {
                    pids.append(", ");
                }
                pids.append(pid);
            }
            if (pids.length() > 0) {
                query.append(" AND param_pref.id IN ( ");
                query.append(pids);
                query.append(" )");
            }
        }
        if (objectType != null && Customer.OBJECT_TYPE.equals(objectType) && paramGroupId > 0) {
            query.append(" AND param_group.group_id=");
            query.append(paramGroupId);
            query.append(" ");
        }
        query.append(" ORDER BY `order`");

        ps = con.prepareStatement(query.toString());
        ps.setString(1, objectType);
        rs = ps.executeQuery();
        while (rs.next()) {
            result.add(getParameterFromRs(rs));
        }
        rs.close();
        ps.close();

        return result;
    }

    private Parameter getParameterFromRs(ResultSet rs) throws SQLException {
        Parameter parameter = new Parameter();

        parameter.setId(rs.getInt("id"));
        parameter.setType(rs.getString("type"));
        parameter.setTitle(rs.getString("title"));
        parameter.setObjectType(rs.getString("object"));
        parameter.setOrder(rs.getInt("order"));
        parameter.setConfig(rs.getString("config"));
        parameter.setComment(rs.getString("comment"));

        return parameter;
    }

    // deprecated
    @Deprecated
    public void getParameterList(Pageable<Parameter> searchResult, String objectType, String filter, int paramGroupId, Set<Integer> parameterIdList)
            throws SQLException {
        log.warndMethod("getParameterList", "searchParameter");
        searchParameter(searchResult, objectType, filter, paramGroupId, parameterIdList);
    }
}