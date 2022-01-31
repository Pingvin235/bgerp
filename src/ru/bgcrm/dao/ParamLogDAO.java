package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_PARAM_LOG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterLogItem;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class ParamLogDAO extends CommonDAO {
    public ParamLogDAO(Connection con) {
        super(con);
    }

    public void insertParamLog(int id, int paramId, int userId, String text) throws SQLException {
        PreparedStatement ps;
        String query;

        // Проверяем последнюю запись по данному параметру, чтобы не записывать неизмененные параметры
        query = "SELECT text FROM " + TABLE_PARAM_LOG + " WHERE object_id=? AND param_id=? "
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
        query = "INSERT INTO" + TABLE_PARAM_LOG + "(dt, object_id, user_id, param_id, text) "
                + "VALUES (NOW(),?,?,?,?)";

        ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.setInt(2, userId);
        ps.setInt(3, paramId);
        ps.setString(4, text);

        ps.executeUpdate();
        ps.close();
    }

    public List<ParameterLogItem> getHistory(int id, List<Parameter> params, boolean offEncryption,
            SearchResult<ParameterLogItem> searchResult) throws BGException {

        PreparedQuery pq = new PreparedQuery(con);
        Page page = searchResult.getPage();
        pq.addQuery(SQL_SELECT_COUNT_ROWS + " dt, object_id, user_id, param_id, text FROM " + TABLE_PARAM_LOG);
        pq.addQuery(" WHERE object_id= ? AND param_id IN ( " + Utils.getObjectIds(params) + " ) ");
        pq.addInt(id);
        pq.addQuery(" ORDER BY dt DESC ");
        pq.addQuery(getPageLimit(page));

        List<ParameterLogItem> result = searchResult.getList();
        try {
            ResultSet rs = pq.executeQuery();
            while (rs.next()) {
                result.add(getLogItemFromRs(rs, offEncryption));
            }
            setRecordCount(page, pq.getPrepared());
            pq.close();

            return result;
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }

    public ParameterLogItem getLastParamChange(int objectId, int paramId) throws BGException {
        try {
            ParameterLogItem result = null;

            String query = "SELECT * FROM " + TABLE_PARAM_LOG + "WHERE object_id=? AND param_id=? "
                    + "ORDER BY dt DESC LIMIT 1";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, objectId);
            ps.setInt(2, paramId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getLogItemFromRs(rs);
            }
            ps.close();

            return result;
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }

    private ParameterLogItem getLogItemFromRs(ResultSet rs, boolean offEncryption) throws SQLException {
        int paramId = rs.getInt("param_id");
        Parameter param = ParameterCache.getParameter(paramId);

        String text = rs.getString("text");
        if ("encrypted".equals(param.getConfigMap().get("encrypt")) && !offEncryption) {
            text = "<ЗНАЧЕНИЕ ЗАШИФРОВАНО>";
        }

        return new ParameterLogItem(TimeUtils.convertTimestampToDate(rs.getTimestamp("dt")), rs.getInt("object_id"),
                rs.getInt("user_id"), paramId, text);
    }

    private ParameterLogItem getLogItemFromRs(ResultSet rs) throws SQLException {
        return getLogItemFromRs(rs, false);
    }
}
