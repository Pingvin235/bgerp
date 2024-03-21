package org.bgerp.dao.param;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bgerp.cache.ParameterCache;
import org.bgerp.model.Pageable;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.param.ParameterLogItem;
import ru.bgcrm.util.Utils;

public class ParamLogDAO extends CommonDAO {
    public ParamLogDAO(Connection con) {
        super(con);
    }

    public void insertParamLog(int id, int paramId, int userId, String text) throws SQLException {
        PreparedStatement ps;
        String query;

        // Проверяем последнюю запись по данному параметру, чтобы не записывать неизмененные параметры
        query = "SELECT text FROM " + Tables.TABLE_PARAM_LOG + " WHERE object_id=? AND param_id=? "
                + "ORDER BY dt DESC LIMIT 1";

        ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, paramId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String loggedText = rs.getString("text");
            if (text.equals(loggedText)) {
                ps.close();
                return;
            }
        }
        ps.close();

        // Запись в лог
        query = "INSERT INTO" + Tables.TABLE_PARAM_LOG + "(dt, object_id, user_id, param_id, text) "
                + "VALUES (CURRENT_TIMESTAMP(4),?,?,?,?)";

        ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, userId);
        ps.setInt(3, paramId);
        ps.setString(4, text);

        ps.executeUpdate();
        ps.close();
    }

    /**
     * Pageable param changes history in reverse time order.
     * @param id entity ID.
     * @param params parameters.
     * @param offEncryption
     * @param result pageable result.
     * @return {@link Pageable#getList()} from {@code result}.
     * @throws SQLException
     */
    public List<ParameterLogItem> getHistory(int id, List<Parameter> params, boolean offEncryption,
            Pageable<ParameterLogItem> result) throws SQLException {

        try (var pq = new PreparedQuery(con)) {
            Page page = result.getPage();
            pq.addQuery(SQL_SELECT_COUNT_ROWS + " dt, object_id, user_id, param_id, text FROM " + Tables.TABLE_PARAM_LOG);
            pq.addQuery(" WHERE object_id= ? AND param_id IN ( " + Utils.getObjectIds(params) + " ) ");
            pq.addInt(id);
            pq.addQuery(" ORDER BY dt DESC ");
            pq.addQuery(getPageLimit(page));

            List<ParameterLogItem> list = result.getList();

            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                list.add(getFromRs(rs, offEncryption));
            }

            setRecordCount(page, pq.getPrepared());

            return list;
        }
    }

    public ParameterLogItem getLastParamChange(int objectId, int paramId) throws SQLException {
        ParameterLogItem result = null;

        String query = "SELECT * FROM " + Tables.TABLE_PARAM_LOG + "WHERE object_id=? AND param_id=? "
                + "ORDER BY dt DESC LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, objectId);
            ps.setInt(2, paramId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getLogItemFromRs(rs);
            }
            ps.close();
        }

        return result;
    }

    private ParameterLogItem getFromRs(ResultSet rs, boolean offEncryption) throws SQLException {
        int paramId = rs.getInt("param_id");
        Parameter param = ParameterCache.getParameter(paramId);

        String text = rs.getString("text");
        if ("encrypted".equals(param.getConfigMap().get("encrypt")) && !offEncryption) {
            text = "<ENCRYPTED>";
        }

        return new ParameterLogItem(rs.getTimestamp("dt"), rs.getInt("object_id"), rs.getInt("user_id"), paramId, text);
    }

    private ParameterLogItem getLogItemFromRs(ResultSet rs) throws SQLException {
        return getFromRs(rs, false);
    }
}
