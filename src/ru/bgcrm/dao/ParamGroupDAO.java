package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.param.ParameterGroup;

public class ParamGroupDAO extends CommonDAO {
    public static final String DIRECTORY_TYPE_PARAMETER_GROUP = "parameterGroup";

    public ParamGroupDAO(Connection con) {
        super(con);
    }

    public List<ParameterGroup> getParameterGroupList(String object) throws SQLException {
        List<ParameterGroup> list = new ArrayList<ParameterGroup>();

        String query = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        query = "SELECT * FROM param_group_title WHERE object=? ORDER BY title";
        ps = con.prepareStatement(query);
        ps.setString(1, object);
        rs = ps.executeQuery();
        while (rs.next()) {
            ParameterGroup parameterGroup = new ParameterGroup();
            setParameterGroupData(rs, parameterGroup);
            list.add(parameterGroup);
        }
        rs.close();

        return list;
    }

    public ParameterGroup getParameterGroup(int parameterGroupId) throws SQLException {
        ParameterGroup parameterGroup = null;

        String query = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        query = "SELECT * FROM param_group_title WHERE id=?";
        ps = con.prepareStatement(query);
        ps.setInt(1, parameterGroupId);
        rs = ps.executeQuery();
        while (rs.next()) {
            parameterGroup = new ParameterGroup();
            setParameterGroupData(rs, parameterGroup);
        }
        rs.close();
        ps.close();

        return parameterGroup;
    }

    public Set<Integer> getParameterIdsForGroup(int parameterGroupId) throws SQLException {
        Set<Integer> ids = new HashSet<Integer>();

        String query = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        query = "SELECT param_id FROM param_group WHERE group_id=?";
        ps = con.prepareStatement(query);
        ps.setInt(1, parameterGroupId);
        rs = ps.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt(1));
        }
        rs.close();
        ps.close();

        return ids;
    }

    public void updateParameterGroup(ParameterGroup parameterGroup) throws SQLException {

        int index = 1;
        String query = null;
        PreparedStatement ps = null;

        if (parameterGroup.getId() <= 0) {
            query = "INSERT INTO param_group_title SET object=?, title=?";
            ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, parameterGroup.getObject());
            ps.setString(index++, parameterGroup.getTitle());
            ps.executeUpdate();
            parameterGroup.setId(lastInsertId(ps));
        } else {
            query = "UPDATE param_group_title SET title=? WHERE id=?";
            ps = con.prepareStatement(query);
            ps.setString(index++, parameterGroup.getTitle());
            ps.setInt(index++, parameterGroup.getId());
            ps.executeUpdate();
        }
        ps.close();

        if (parameterGroup.getParameterIds() != null) {
            query = "DELETE FROM param_group WHERE group_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, parameterGroup.getId());
            ps.executeUpdate();
            ps.close();

            query = "INSERT INTO param_group SET group_id=?, param_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, parameterGroup.getId());
            for (Integer parameterid : parameterGroup.getParameterIds()) {
                ps.setInt(2, parameterid);
                ps.executeUpdate();
            }
            ps.close();
        }
    }

    public void deleteParameterGroup(int id) throws BGException {
        try {
            String query = "DELETE FROM param_group_title WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            query = "DELETE FROM param_group WHERE group_id=?";
            ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    private void setParameterGroupData(ResultSet rs, ParameterGroup parameterGroup) throws SQLException {
        parameterGroup.setId(rs.getInt("id"));
        parameterGroup.setTitle(rs.getString("title"));
    }
}