package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_PARAM_GROUP;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT_VALUE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST_VALUE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_PREF;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TREE_VALUE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.model.Pageable;
import org.bgerp.model.base.tree.IdStringTitleTreeItem;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterHistory;
import ru.bgcrm.util.Utils;

public class ParamDAO extends CommonDAO {
    public static final String DIRECTORY_TYPE_PARAMETER = "parameter";

    public ParamDAO(Connection connection) {
        super(connection);
    }

    // TODO: Убрать конструктор.
    public ParamDAO(Connection con, int userId) {
        super(con);
    }

    public Parameter getParameter(int id) throws SQLException {
        Parameter parameter = null;

        String query = "SELECT * FROM param_pref WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            parameter = getParameterFromRs(rs);

            if (Parameter.TYPE_LIST.equals(parameter.getType())) {
                parameter.setValuesConfig(getListParamValuesConfig(id));
                // parameter.setListValues( getParamListValueForParamId( id ) );
            } else if (Parameter.TYPE_TREE.equals(parameter.getType())) {
                parameter.setValuesConfig(getTreeParamValuesConfig(id));
            }

            else if (Parameter.TYPE_LISTCOUNT.equals(parameter.getType())) {
                parameter.setValuesConfig(getListCountParamValuesConfig(id));
            }
        }
        ps.close();

        return parameter;
    }

    private String getListCountParamValuesConfig(int paramId) throws SQLException {
        StringBuilder query = new StringBuilder();
        ResultSet rs = null;
        PreparedStatement ps = null;
        query.append("SELECT id, title FROM ");
        query.append(TABLE_PARAM_LISTCOUNT_VALUE);
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

    public Map<Integer, IdStringTitleTreeItem> getTreeParamValuesMap() throws SQLException {
        Map<Integer, IdStringTitleTreeItem> result = new HashMap<Integer, IdStringTitleTreeItem>();

        StringBuilder query = new StringBuilder(200);

        query.append("SELECT DISTINCT(param_id) FROM ");
        query.append(TABLE_PARAM_TREE_VALUE);
        query.append("ORDER BY id");

        PreparedStatement ps = con.prepareStatement(query.toString());

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Integer paramId = rs.getInt("param_id");
            result.put(paramId, getTreeParamValues(paramId));
        }
        ps.close();

        return result;
    }

    private String getTreeParamValuesConfig(int paramId) throws SQLException {
        IdStringTitleTreeItem root = getTreeParamValues(paramId);
        StringBuilder config = new StringBuilder(getTreeConfig(root, ""));

        return config.toString();
    }

    private String getTreeConfig(IdStringTitleTreeItem root, String prefix) {
        StringBuilder config = new StringBuilder();

        if (Utils.notBlankString(root.getId())) {
            config.append(prefix + "=" + root.getTitle());
            config.append("\n");
        }

        List<IdStringTitleTreeItem> children = root.getChildren();
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

    private IdStringTitleTreeItem getTreeParamValues(int paramId) throws SQLException {
        IdStringTitleTreeItem root = new IdStringTitleTreeItem();

        String query = "SELECT id, parent_id, title FROM " + TABLE_PARAM_TREE_VALUE + " WHERE param_id=? ORDER BY id";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, paramId);
        ResultSet rs = ps.executeQuery();

        List<IdStringTitleTreeItem> items = new ArrayList<>();
        while (rs.next()) {
            items.add(new IdStringTitleTreeItem(rs.getString("id"), rs.getString("title"), rs.getString("parent_id")));
        }
        ps.close();

        Collections.sort(items, (a, b) -> {
            Iterator<Integer> idsA = a.getIds().iterator();
            Iterator<Integer> idsB = b.getIds().iterator();

            if (idsA.hasNext() || idsB.hasNext()) {
                if (!idsA.hasNext())
                    return -1;
                if (!idsB.hasNext())
                    return 1;

                Integer nextA = idsA.next(), nextB = idsB.next();
                if (nextA != nextB)
                    return nextA - nextB;
            }
            return 0;
        });

        items.forEach((item) -> {
            IdStringTitleTreeItem child = root.getChild(item.getParentId());
            if (child != null) {
                child.addChild(item);
            } else {
                root.addChild(item);
            }
        });

        return root;
    }

    public void updateParameter(Parameter parameter) throws SQLException {
        int index = 1;
        String query = null;
        PreparedStatement ps = null;

        if (parameter.getId() < 0) {
            query = "INSERT INTO param_pref SET object=?, type=?, title=?, `order`=?, script=?, config=?, comment=?";
            ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, parameter.getObject());
            ps.setString(index++, parameter.getType());
            ps.setString(index++, parameter.getTitle());
            ps.setInt(index++, parameter.getOrder());
            ps.setString(index++, parameter.getScript());
            ps.setString(index++, parameter.getConfig());
            ps.setString(index++, parameter.getComment());
            ps.executeUpdate();
            parameter.setId(lastInsertId(ps));
        } else {
            query = "UPDATE param_pref SET title=?, `order`=?, script=?, config=?, comment=? WHERE id=?";
            ps = con.prepareStatement(query);
            ps.setString(index++, parameter.getTitle());
            ps.setInt(index++, parameter.getOrder());
            ps.setString(index++, parameter.getScript());
            ps.setString(index++, parameter.getConfig());
            ps.setString(index++, parameter.getComment());
            ps.setInt(index++, parameter.getId());
            ps.executeUpdate();
        }
        ps.close();

        if (Parameter.TYPE_LIST.equals(parameter.getType())) {
            query = "DELETE FROM " + TABLE_PARAM_LIST_VALUE + " WHERE param_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, parameter.getId());
            ps.executeUpdate();
            ps.close();

            query = "INSERT INTO " + TABLE_PARAM_LIST_VALUE + " (id, title, param_id) VALUES (?,?,?)";
            ps = con.prepareStatement(query);
            ps.setInt(3, parameter.getId());

            for (Map.Entry<Integer, String> me : convertListValuesConfigToMap(parameter.getValuesConfig()).entrySet()) {
                ps.setInt(1, me.getKey());
                ps.setString(2, me.getValue());
                ps.executeUpdate();
            }
            ps.close();
        } else if (Parameter.TYPE_TREE.equals(parameter.getType())) {
            query = "DELETE FROM " + TABLE_PARAM_TREE_VALUE + " WHERE param_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, parameter.getId());
            ps.executeUpdate();
            ps.close();

            query = "INSERT INTO " + TABLE_PARAM_TREE_VALUE + " (id, parent_id, title, param_id) VALUES (?,?,?,?)";
            ps = con.prepareStatement(query);
            ps.setInt(4, parameter.getId());

            for (IdStringTitleTreeItem node : convertTreeValuesConfigToNodeList(parameter.getValuesConfig())) {
                ps.setString(1, node.getId());
                ps.setString(2, node.getParentId());
                ps.setString(3, node.getTitle());
                ps.executeUpdate();
            }
            ps.close();
        } else if (Parameter.TYPE_LISTCOUNT.equals(parameter.getType())) {
            query = "DELETE FROM " + TABLE_PARAM_LISTCOUNT_VALUE + " WHERE param_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, parameter.getId());
            ps.executeUpdate();
            ps.close();

            query = "INSERT INTO " + TABLE_PARAM_LISTCOUNT_VALUE + " (id, title, param_id) VALUES (?,?,?)";
            ps = con.prepareStatement(query);
            ps.setInt(3, parameter.getId());

            for (Map.Entry<Integer, String> me : convertListValuesConfigToMap(parameter.getValuesConfig()).entrySet()) {
                ps.setInt(1, me.getKey());
                ps.setString(2, me.getValue());
                ps.executeUpdate();
            }
            ps.close();
        }
    }

    private List<IdStringTitleTreeItem> convertTreeValuesConfigToNodeList(String valuesConfig) {
        List<IdStringTitleTreeItem> nodes = new ArrayList<IdStringTitleTreeItem>();
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

    public void deleteParameter(int id) throws BGException {
        try {
            String query = "DELETE FROM " + TABLE_PARAM_PREF + " WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            query = "DELETE FROM " + TABLE_PARAM_LIST_VALUE + " WHERE param_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public Map<String, List<Parameter>> getParameterMapByObjectType() throws SQLException {
        Map<String, List<Parameter>> result = new HashMap<String, List<Parameter>>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT param_pref.* FROM ");
        query.append(TABLE_PARAM_PREF);
        query.append("ORDER BY object, `order`");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Parameter param = getParameterFromRs(rs);

            List<Parameter> paramList = result.get(param.getObject());
            if (paramList == null) {
                paramList = new ArrayList<Parameter>();
                result.put(param.getObject(), paramList);
            }

            paramList.add(param);
        }
        ps.close();

        return result;
    }

    public Map<Integer, Set<Integer>> getParameterIdsByGroupIds() throws SQLException {
        Map<Integer, Set<Integer>> result = new HashMap<Integer, Set<Integer>>();

        StringBuilder query = new StringBuilder(200);
        query.append("SELECT group_id, param_id FROM ");
        query.append(TABLE_PARAM_GROUP);

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int groupId = rs.getInt(1);

            Set<Integer> paramIds = result.get(groupId);
            if (paramIds == null) {
                paramIds = new HashSet<Integer>();
                result.put(groupId, paramIds);
            }
            paramIds.add(rs.getInt(2));
        }
        ps.close();

        return result;
    }

    public Map<Integer, List<IdTitle>> getListParamValuesMap() throws SQLException {
        Map<Integer, List<IdTitle>> result = new LinkedHashMap<Integer, List<IdTitle>>();

        StringBuilder query = new StringBuilder(200);

        query.append("SELECT id, param_id, title FROM ");
        query.append(TABLE_PARAM_LIST_VALUE);
        query.append("UNION SELECT id, param_id, title FROM ");
        query.append(TABLE_PARAM_LISTCOUNT_VALUE);

        PreparedStatement ps = con.prepareStatement(query.toString());

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int paramId = rs.getInt(2);

            List<IdTitle> valueList = result.get(paramId);
            if (valueList == null) {
                result.put(paramId, valueList = new ArrayList<IdTitle>());
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
        query.append(TABLE_PARAM_LIST_VALUE);
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
        Map<Integer, String> listValuesMap = new LinkedHashMap<Integer, String>();

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

    /*используется в DirectoryAction тащит за собой public List<Parameter> getParameterList( String objectType,
     int paramGroupId, Set<Integer> parameterIdList ) ПЕРЕДЕЛАТЬ!*/
    public List<Parameter> getParameterList(String objectType, int paramGroupId) throws SQLException {
        return getParameterList(objectType, paramGroupId, null);
    }

    public void getParameterList(Pageable<Parameter> searchResult, String objectType, String titleOrCommentOrConfigFilter, int paramGroupId,
            Set<Integer> parameterIdList) throws BGException {
        try {
            Page page = searchResult.getPage();

            PreparedQuery psDelay = new PreparedQuery(con);
            StringBuilder query = new StringBuilder();

            query.append("SELECT SQL_CALC_FOUND_ROWS param_pref.* FROM ");
            query.append(TABLE_PARAM_PREF);
            if (objectType != null && Customer.OBJECT_TYPE.equals(objectType) && paramGroupId > 0) {
                query.append(" LEFT JOIN param_group ON param_pref.id=param_group.param_id");
            }
            query.append(" WHERE object=?");

            if (Utils.notBlankString(titleOrCommentOrConfigFilter)) {
                query.append(" AND ( param_pref.title LIKE ? OR param_pref.comment LIKE ? OR param_pref.config LIKE ?)");
            }

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
            query.append(" ORDER BY `order`, title");
            query.append(getPageLimit(page));
            psDelay.addQuery(query.toString());

            psDelay.addString(objectType);
            if (Utils.notBlankString(titleOrCommentOrConfigFilter)) {
                psDelay.addString(titleOrCommentOrConfigFilter);
                psDelay.addString(titleOrCommentOrConfigFilter);
                psDelay.addString(titleOrCommentOrConfigFilter);
            }

            ResultSet rs = psDelay.executeQuery();

            while (rs.next()) {
                searchResult.getList().add(getParameterFromRs(rs));
            }

            if (page != null) {
                page.setRecordCount(foundRows(psDelay.getPrepared()));
            }

            psDelay.close();
        } catch (Exception e) {
            throw new BGException(e);
        }
    }

    public List<Parameter> getParameterList(String objectType, int paramGroupId, Set<Integer> parameterIdList) throws SQLException {
        List<Parameter> result = new ArrayList<Parameter>();

        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();

        query.append("SELECT param_pref.* FROM ");
        query.append(TABLE_PARAM_PREF);
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

    @Deprecated
    public List<ParameterHistory> getParameterHistory(String object, Parameter parameter, int id) throws SQLException {
        return Collections.emptyList();
    }

    private Parameter getParameterFromRs(ResultSet rs) throws SQLException {
        Parameter parameter = new Parameter();

        parameter.setId(rs.getInt("id"));
        parameter.setType(rs.getString("type"));
        parameter.setTitle(rs.getString("title"));
        parameter.setObject(rs.getString("object"));
        parameter.setOrder(rs.getInt("order"));
        parameter.setScript(rs.getString("script"));
        parameter.setConfig(rs.getString("config"));
        parameter.setComment(rs.getString("comment"));

        return parameter;
    }
}