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
import org.bgerp.model.Pageable;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.plugin.dispatch.model.Dispatch;
import ru.bgcrm.plugin.dispatch.model.DispatchMessage;
import ru.bgcrm.util.Utils;

public class DispatchDAO extends CommonDAO {
    public DispatchDAO(Connection con) {
        super(con);
    }

    public void searchDispatch(Pageable<Dispatch> result) throws SQLException {
        String query = SQL_SELECT_ALL_FROM + Tables.TABLE_DISPATCH + SQL_ORDER_BY + "title";

        PreparedStatement ps = con.prepareStatement(query);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.getList().add(getDispatchFromRs(rs));
        }
        ps.close();
    }

    public void dispatchUpdate(Dispatch dispatch) throws SQLException {
        update(dispatch, new RecordUpdater<>() {
            @Override
            public String getInsertQuery() throws SQLException {
                return SQL_INSERT_INTO + Tables.TABLE_DISPATCH + " (title, comment) VALUES (?,?)";
            }

            @Override
            public Pair<String, Integer> getUpdateQuery() throws SQLException {
                return new Pair<>(SQL_UPDATE + Tables.TABLE_DISPATCH + " SET title=?, comment=? WHERE id=?", 3);
            }

            @Override
            public void fillCommonFields(Dispatch record, PreparedStatement ps) throws SQLException {
                ps.setString(1, record.getTitle());
                ps.setString(2, record.getComment());
            }
        });
    }

    public Dispatch dispatchGet(int id) throws SQLException {
        return getById(Tables.TABLE_DISPATCH, id, new ObjectExtractor<>() {
            @Override
            public Dispatch extract(ResultSet rs) throws SQLException {
                return getDispatchFromRs(rs);
            }
        });
    }

    public void dispatchDelete(int id) throws SQLException {
        deleteById(Tables.TABLE_DISPATCH, id);
    }

    public List<Dispatch> dispatchList(Set<Integer> ids) throws SQLException {
        List<Dispatch> result = new ArrayList<>();

        String query = "SELECT * FROM " + Tables.TABLE_DISPATCH;
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

        return result;
    }

    public void dispatchUpdateAccountCounts() throws SQLException {
        String query = "UPDATE " + Tables.TABLE_DISPATCH + " AS d "
                + "SET account_count=(SELECT COUNT(*) FROM dispatch_account_subscription AS s WHERE s.dispatch_id=d.id)";

        Statement st = con.createStatement();
        st.executeUpdate(query);
        st.close();
    }

    public List<Dispatch> accountSubscriptions(String email) throws SQLException {
        List<Dispatch> result = new ArrayList<>();

        String query = "SELECT d.* FROM " + Tables.TABLE_DISPATCH + " AS d " + "INNER JOIN " + Tables.TABLE_ACCOUNT_SUBSCRIPTION
                + " AS subscr ON d.id=subscr.dispatch_id AND subscr.account=?" + "ORDER BY d.title";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(getDispatchFromRs(rs));
        }
        ps.close();

        return result;
    }

    public void accountSubscriptionUpdate(String email, Set<Integer> dispatchIds) throws SQLException {
        updateIds(Tables.TABLE_ACCOUNT_SUBSCRIPTION, "account", "dispatch_id", email, dispatchIds);
        dispatchUpdateAccountCounts();
    }

    public void messageSearch(Pageable<DispatchMessage> result, Boolean sent) throws SQLException {
        PreparedQuery pq = new PreparedQuery(con);
        pq.addQuery("SELECT * FROM " + Tables.TABLE_DISPATCH_MESSAGE);
        if (sent != null) {
            pq.addQuery("WHERE sent_dt ");
            if (!sent) {
                pq.addQuery("IS NULL");
            }
        }
        pq.addQuery(" ORDER BY create_dt DESC");

        ResultSet rs = pq.executeQuery();
        while (rs.next()) {
            result.getList().add(getMessageFromRs(rs));
        }
        pq.close();
    }

    public List<DispatchMessage> messageUnsentList() throws SQLException {
        List<DispatchMessage> result = new ArrayList<>();

        String query = "SELECT * FROM " + Tables.TABLE_DISPATCH_MESSAGE + "WHERE ready AND sent_dt IS NULL " + "ORDER BY create_dt";
        PreparedStatement ps = con.prepareStatement(query);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(getMessageFromRs(rs));
        }
        ps.close();

        return result;
    }

    public DispatchMessage messageGet(int id) throws SQLException {
        return getById(Tables.TABLE_DISPATCH_MESSAGE, id, new ObjectExtractor<>() {
            @Override
            public DispatchMessage extract(ResultSet rs) throws SQLException {
                return getMessageFromRs(rs);
            }
        });
    }

    public void messageDelete(int id) throws SQLException {
        deleteById(Tables.TABLE_DISPATCH_MESSAGE, id);
    }

    public List<String> messageAccountList(int messageId) throws SQLException {
        List<String> result = new ArrayList<>();

        String query = "SELECT DISTINCT account FROM " + Tables.TABLE_ACCOUNT_SUBSCRIPTION + "AS subscr " + "INNER JOIN "
                + Tables.TABLE_DISPATCH_MESSAGE_DISPATCH + " AS mess_disp ON subscr.dispatch_id=mess_disp.dispatch_id AND mess_disp.message_id=?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, messageId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        ps.close();

        return result;
    }

    private DispatchMessage getMessageFromRs(ResultSet rs) throws SQLException {
        DispatchMessage message = new DispatchMessage();

        message.setId(rs.getInt("id"));
        message.setTitle(rs.getString("title"));
        message.setText(rs.getString("text"));
        message.setDispatchIds(Utils.toIntegerSet(rs.getString("dispatch_ids")));
        message.setReady(rs.getBoolean("ready"));
        message.setCreateTime(rs.getTimestamp("create_dt"));
        message.setSentTime(rs.getTimestamp("sent_dt"));

        return message;
    }

    public void messageUpdate(DispatchMessage message) throws Exception {
        update(message, new RecordUpdater<>() {
            @Override
            public String getInsertQuery() throws SQLException {
                return "INSERT INTO " + Tables.TABLE_DISPATCH_MESSAGE + " (dispatch_ids, title, text, ready, create_dt, sent_dt) " + "VALUES (?,?,?,?,?,?)";
            }

            @Override
            public Pair<String, Integer> getUpdateQuery() throws SQLException {
                return new Pair<>(
                        "UPDATE  " + Tables.TABLE_DISPATCH_MESSAGE + " SET dispatch_ids=?, title=?, text=?, ready=?, create_dt=?, sent_dt=? " + "WHERE id=?",
                        7);
            }

            @Override
            public void fillCommonFields(DispatchMessage message, PreparedStatement ps) throws SQLException {
                ps.setString(1, Utils.toString(message.getDispatchIds()));
                ps.setString(2, message.getTitle());
                ps.setString(3, message.getText());
                ps.setBoolean(4, message.isReady());
                ps.setTimestamp(5, TimeConvert.toTimestamp(message.getCreateTime()));
                ps.setTimestamp(6, TimeConvert.toTimestamp(message.getSentTime()));
            }
        });

        updateIds(Tables.TABLE_DISPATCH_MESSAGE_DISPATCH, "message_id", "dispatch_id", message.getId(), message.getDispatchIds());
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
