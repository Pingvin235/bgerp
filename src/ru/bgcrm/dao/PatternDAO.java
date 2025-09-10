package ru.bgcrm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.dao.expression.ParamExpressionObject;
import org.bgerp.util.text.PatternFormatter;

import ru.bgcrm.model.param.Pattern;
import ru.bgcrm.util.Utils;

public class PatternDAO extends CommonDAO {
    private static final String TABLE = " object_title_pattern ";

    /**
     * Generation of title from a pattern with parameter values
     * @param params param accessor
     * @param pattern the {@link PatternFormatter} pattern with <pre>${param_&lt;PARAM_ID&gt;}</pre> substitutions
     * @return the formatted title
     */
    public static String format(ParamExpressionObject params, String pattern) {
        return PatternFormatter.processPattern(pattern, variable -> {
            int paramId = Utils.parseInt(StringUtils.substringAfter(variable, "param_"));
            if (paramId > 0)
                return params.val(paramId);
            return "???";
        });
    }

    public PatternDAO(Connection con) {
        super(con);
    }

    public List<Pattern> getPatternList(String object) throws SQLException {
        List<Pattern> result = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement ps = null;
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM" + TABLE + "WHERE object=? ORDER BY title");
        ps = con.prepareStatement(query.toString());
        ps.setString(1, object);
        rs = ps.executeQuery();
        while (rs.next()) {
            Pattern pattern = new Pattern();
            setPatternData(pattern, rs);
            result.add(pattern);
        }
        rs.close();
        ps.close();

        return result;
    }

    public Pattern getPattern(int id) throws SQLException {
        Pattern pattern = null;

        String query = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        query = "SELECT * FROM" + TABLE + "WHERE id=?";
        ps = con.prepareStatement(query);
        ps.setInt(1, id);
        rs = ps.executeQuery();
        while (rs.next()) {
            pattern = new Pattern();
            setPatternData(pattern, rs);
        }
        rs.close();
        ps.close();

        return pattern;
    }

    public void updatePattern(Pattern pattern) throws SQLException {
        int index = 1;
        String query = null;
        PreparedStatement ps = null;

        if (pattern.getId() <= 0) {
            query = "INSERT INTO" + TABLE + "SET object=?, title=?, pattern=?";
            ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, pattern.getObject());
            ps.setString(index++, pattern.getTitle());
            ps.setString(index++, pattern.getPattern());
            ps.executeUpdate();
            pattern.setId(lastInsertId(ps));
        } else {
            query = "UPDATE" + TABLE + "SET title=?, pattern=? WHERE id=?";
            ps = con.prepareStatement(query);
            ps.setString(index++, pattern.getTitle());
            ps.setString(index++, pattern.getPattern());
            ps.setInt(index++, pattern.getId());
            ps.executeUpdate();
        }
        ps.close();
    }

    public void deletePattern(int id) throws SQLException {
        String query = "DELETE FROM" + TABLE + "WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    private void setPatternData(Pattern pattern, ResultSet rs) throws SQLException {
        pattern.setId(rs.getInt("id"));
        pattern.setTitle(rs.getString("title"));
        pattern.setPattern(rs.getString("pattern"));
    }
}