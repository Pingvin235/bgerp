package ru.bgcrm.plugin.dispatch.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.dispatch.model.Dispatch;
import ru.bgcrm.plugin.dispatch.model.DispatchMessage;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;

public class DispatchDAO extends CommonDAO {
    private static final String TABLE_DISPATCH = " dispatch ";
    private static final String TABLE_DISPATCH_MESSAGE = " dispatch_message ";
    private static final String TABLE_DISPATCH_MESSAGE_DISPATCH = " dispatch_message_dispatch ";
    private static final String TABLE_ACCOUNT_SUBSCRIPTION = " dispatch_account_subscription ";

    public DispatchDAO(Connection con) {
        super(con);
    }

    public void searchDispatch(SearchResult<Dispatch> result) throws BGException {
        try {
            String query = "SELECT * FROM " + TABLE_DISPATCH + " ORDER BY title";

            PreparedStatement ps = con.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.getList().add(getDispatchFromRs(rs));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void dispatchUpdate(Dispatch dispatch) throws BGException {
        update(dispatch, new RecordUpdater<Dispatch>() {
            @Override
            public String getInsertQuery() throws SQLException {
                return SQL_INSERT + TABLE_DISPATCH + " (title, comment) VALUES (?,?)";
            }

            @Override
            public Pair<String, Integer> getUpdateQuery() throws SQLException {
                return new Pair<String, Integer>(SQL_UPDATE + TABLE_DISPATCH + " SET title=?, comment=? WHERE id=?", 3);
            }

            @Override
            public void fillCommonFields(Dispatch record, PreparedStatement ps) throws SQLException {
                ps.setString(1, record.getTitle());
                ps.setString(2, record.getComment());
            }
        });
    }

    public Dispatch dispatchGet(int id) throws BGException {
        return getById(TABLE_DISPATCH, id, new ObjectExtractor<Dispatch>() {
            @Override
            public Dispatch extract(ResultSet rs) throws SQLException {
                return getDispatchFromRs(rs);
            }
        });
    }

    public void dispatchDelete(int id) throws BGException {
        deleteById(TABLE_DISPATCH, id);
    }

    public List<Dispatch> dispatchList(Set<Integer> ids) throws BGException {
        List<Dispatch> result = new ArrayList<Dispatch>();

        try {
            String query = "SELECT * FROM " + TABLE_DISPATCH;
            if (CollectionUtils.isNotEmpty(ids)) {
                query += "WHERE id IN ( " + Utils.toString(ids) + ") ";
            }
            query += "ORDER BY title";

            PreparedStatement ps = con.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getDispatchFromRs(rs));
            }
            ps.close();
        } catch (Exception e) {
            throw new BGException(e);
        }

        return result;
    }

    public void dispatchUpdateAccountCounts() throws BGException {
        try {
            String query = "UPDATE " + TABLE_DISPATCH + " AS d "
                    + "SET account_count=(SELECT COUNT(*) FROM dispatch_account_subscription AS s WHERE s.dispatch_id=d.id)";

            Statement st = con.createStatement();
            st.executeUpdate(query);
            st.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<Dispatch> accountSubsriptionList(String email) throws BGException {
        List<Dispatch> result = new ArrayList<Dispatch>();

        try {
            String query = "SELECT d.* FROM " + TABLE_DISPATCH + " AS d " + "INNER JOIN " + TABLE_ACCOUNT_SUBSCRIPTION
                    + " AS subscr ON d.id=subscr.dispatch_id AND subscr.account=?" + "ORDER BY d.title";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getDispatchFromRs(rs));
            }
            ps.close();
        } catch (Exception e) {
            throw new BGException(e);
        }

        return result;
    }

    public void accountSubsriptionUpdate(String email, Set<Integer> dispatchIds) throws BGException {
        updateIds(TABLE_ACCOUNT_SUBSCRIPTION, "account", "dispatch_id", email, dispatchIds);
        dispatchUpdateAccountCounts();
    }

    public void messageSearch(SearchResult<DispatchMessage> result, Boolean sent) throws BGException {
        try {
            PreparedDelay pd = new PreparedDelay(con);
            pd.addQuery("SELECT * FROM " + TABLE_DISPATCH_MESSAGE);
            if (sent != null) {
                pd.addQuery("WHERE sent_dt ");
                if (!sent) {
                    pd.addQuery("IS NULL");
                }
            }
            pd.addQuery(" ORDER BY create_dt DESC");

            ResultSet rs = pd.executeQuery();
            while (rs.next()) {
                result.getList().add(getMessageFromRs(rs));
            }
            pd.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<DispatchMessage> messageUnsentList() throws BGException {
        List<DispatchMessage> result = new ArrayList<DispatchMessage>();

        try {
            String query = "SELECT * FROM " + TABLE_DISPATCH_MESSAGE + "WHERE ready AND sent_dt IS NULL " + "ORDER BY create_dt";
            PreparedStatement ps = con.prepareStatement(query);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getMessageFromRs(rs));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public DispatchMessage messageGet(int id) throws BGException {
        return getById(TABLE_DISPATCH_MESSAGE, id, new ObjectExtractor<DispatchMessage>() {
            @Override
            public DispatchMessage extract(ResultSet rs) throws SQLException, BGException {
                return getMessageFromRs(rs);
            }
        });
    }

    public void messageDelete(int id) throws BGException {
        deleteById(TABLE_DISPATCH_MESSAGE, id);
    }

    public List<String> messageAccountList(int messageId) throws BGException {
        List<String> result = new ArrayList<String>();

        try {
            String query = "SELECT DISTINCT account FROM " + TABLE_ACCOUNT_SUBSCRIPTION + "AS subscr " + "INNER JOIN "
                    + TABLE_DISPATCH_MESSAGE_DISPATCH + " AS mess_disp ON subscr.dispatch_id=mess_disp.dispatch_id AND mess_disp.message_id=?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, messageId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    private DispatchMessage getMessageFromRs(ResultSet rs) throws SQLException {
        DispatchMessage message = new DispatchMessage();

        message.setId(rs.getInt("id"));
        message.setTitle(rs.getString("title"));
        message.setText(rs.getString("text"));
        message.setDispatchIds(Utils.toIntegerSet(rs.getString("dispatch_ids")));
        message.setReady(rs.getBoolean("ready"));
        message.setCreateTime(TimeUtils.convertTimestampToDate(rs.getTimestamp("create_dt")));
        message.setSentTime(TimeUtils.convertTimestampToDate(rs.getTimestamp("sent_dt")));

        return message;
    }

    public void messageUpdate(DispatchMessage message) throws BGException {
        update(message, new RecordUpdater<DispatchMessage>() {
            @Override
            public String getInsertQuery() throws SQLException {
                return "INSERT INTO " + TABLE_DISPATCH_MESSAGE + " (dispatch_ids, title, text, ready, create_dt, sent_dt) " + "VALUES (?,?,?,?,?,?)";
            }

            @Override
            public Pair<String, Integer> getUpdateQuery() throws SQLException {
                return new Pair<String, Integer>(
                        "UPDATE  " + TABLE_DISPATCH_MESSAGE + " SET dispatch_ids=?, title=?, text=?, ready=?, create_dt=?, sent_dt=? " + "WHERE id=?",
                        7);
            }

            @Override
            public void fillCommonFields(DispatchMessage message, PreparedStatement ps) throws SQLException {
                ps.setString(1, Utils.toString(message.getDispatchIds()));
                ps.setString(2, message.getTitle());
                ps.setString(3, message.getText());
                ps.setBoolean(4, message.isReady());
                ps.setTimestamp(5, TimeUtils.convertDateToTimestamp(message.getCreateTime()));
                ps.setTimestamp(6, TimeUtils.convertDateToTimestamp(message.getSentTime()));
            }
        });

        updateIds(TABLE_DISPATCH_MESSAGE_DISPATCH, "message_id", "dispatch_id", message.getId(), message.getDispatchIds());
    }

    private Dispatch getDispatchFromRs(ResultSet rs) throws SQLException {
        Dispatch dispatch = new Dispatch();

        dispatch.setId(rs.getInt("id"));
        dispatch.setTitle(rs.getString("title"));
        dispatch.setComment(rs.getString("comment"));
        dispatch.setAccountCount(rs.getInt("account_count"));

        return dispatch;
    }
}
