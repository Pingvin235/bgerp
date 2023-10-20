package ru.bgcrm.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.model.IfaceState;
import ru.bgcrm.struts.form.DynActionForm;

public class IfaceStateDAO extends CommonDAO{
    private static final Log log = Log.getLog();

    public static final String TABLE_NAME = " iface_state ";

    public IfaceStateDAO(Connection con) {
        super(con);
    }

    public Map<String, IfaceState> getIfaceStates(String objectType, int objectId) throws SQLException {
        Map<String, IfaceState> result = new HashMap<>();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE object_type=? AND object_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, objectType);
        ps.setInt(2, objectId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            IfaceState state = new IfaceState(rs.getString("iface_id"), rs.getString("state"));
            result.put(state.getIfaceId(), state);
        }
        ps.close();

        return result;
    }

    public void updateIfaceState(IfaceState state) throws SQLException {
        String query =
                SQL_INSERT + TABLE_NAME +
                "SET object_type=?, object_id=?, iface_id=?, state=?" +
                SQL_ON_DUP_KEY_UPDATE +
                "state=?";
        PreparedQuery pq = new PreparedQuery(con, query);
        pq.addString(state.getObjectType());
        pq.addInt(state.getObjectId());
        pq.addString(state.getIfaceId());
        pq.addString(state.getState());
        pq.addString(state.getState());
        pq.executeUpdate();
        pq.close();
    }

    public void compareAndUpdateState(IfaceState currentState, IfaceState newState, DynActionForm form) throws SQLException {
        if (Setup.getSetup().getBoolean("db.readonly", false)) {
            log.debug("Skip compareAndUpdateState for db.readonly=1");
            return;
        }

        boolean needBeUpdated = currentState.getState() == null || !currentState.getState().equals(newState.getState());
        if (needBeUpdated) {
            updateIfaceState(newState);
            try {
                log.debug("Update iface state to: {}", newState.getState());

                form.setRequestUrl(form.getRequestUrl() +
                        "&!" + IfaceState.REQUEST_PARAM_STATE + "=" +
                        URLEncoder.encode(newState.getState(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new SQLException(e);
            }
        }
    }
}