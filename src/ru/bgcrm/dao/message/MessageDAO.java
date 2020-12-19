package ru.bgcrm.dao.message;

import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_PROCESS_MESSAGE_STATE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.config.IsolationConfig;
import ru.bgcrm.model.config.TagConfig;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;
import ru.bgcrm.util.sql.SQLUtils;

public class MessageDAO extends CommonDAO {
    private static final String TABLE_MESSAGE_TAG = "message_tag";

    private final User user;
    
    public MessageDAO(Connection con) {
        super(con);
        this.user = User.USER_SYSTEM;
    }

    public MessageDAO(Connection con, User user) {
        super(con);
        this.user = user;
    }

    public Message getMessageById(int id) throws BGException {
        Message result = null;

        try {
            String query = "SELECT m.*, p.* FROM " + TABLE_MESSAGE + " AS m " + "LEFT JOIN " + TABLE_PROCESS
                    + " AS p ON m.process_id=p.id " + "WHERE m.id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getMessageFromRs(rs, "m.");
                if (rs.getInt("p.id") > 0) {
                    result.setProcess(ProcessDAO.getProcessFromRs(rs, "p."));
                }
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public Message getMessageBySystemId(int typeId, String systemId) throws BGException {
        Message result = null;

        try {
            String query = "SELECT m.*, p.* FROM " + TABLE_MESSAGE + " AS m " + "LEFT JOIN " + TABLE_PROCESS
                    + " AS p ON m.process_id=p.id " + "WHERE m.type_id=? AND m.system_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, typeId);
            ps.setString(2, systemId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getMessageFromRs(rs, "m.");
                if (rs.getInt("p.id") > 0) {
                    result.setProcess(ProcessDAO.getProcessFromRs(rs, "p."));
                }
            }
            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }

        return result;
    }

    public void updateMessage(Message message) throws BGException {
        try {
            PreparedStatement ps = null;

            if (message.getId() <= 0) {
                String query = "INSERT INTO " + TABLE_MESSAGE
                        + "(system_id, type_id, direction, `from`, `to`, subject, text, user_id, processed, process_id, from_dt, to_dt, attach_data) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            } else {
                String query = "UPDATE " + TABLE_MESSAGE
                        + "SET system_id=?, type_id=?, direction=?, `from`=?, `to`=?, subject=?, text=?, user_id=?, processed=?, process_id=?, from_dt=?, to_dt=?, attach_data=? "
                        + "WHERE id=?";
                ps = con.prepareStatement(query);
            }

            int index = 1;
            ps.setString(index++, message.getSystemId());
            ps.setInt(index++, message.getTypeId());
            ps.setInt(index++, message.getDirection());
            ps.setString(index++, message.getFrom());
            ps.setString(index++, message.getTo());
            ps.setString(index++, message.getSubject());
            ps.setString(index++, message.getText());
            ps.setInt(index++, message.getUserId());
            ps.setBoolean(index++, message.isProcessed());
            ps.setInt(index++, message.getProcessId());
            ps.setTimestamp(index++, TimeUtils.convertDateToTimestamp(message.getFromTime()));
            ps.setTimestamp(index++, TimeUtils.convertDateToTimestamp(message.getToTime()));
            ps.setString(index++, FileData.serialize(message.getAttachList()));

            if (message.getId() > 0) {
                ps.setInt(index++, message.getId());
            }

            ps.executeUpdate();

            if (message.getId() <= 0) {
                message.setId(SQLUtils.lastInsertId(ps));
            }
            ps.close();

            updateProcessLastMessageTime(message);
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }
    
    public void updateMessageProcess(Message message) throws BGException {
        try {
            String query = "UPDATE " + TABLE_MESSAGE + "SET user_id=?, processed=?, to_dt=?, process_id=? "
                    + "WHERE id=?";
            PreparedStatement ps = con.prepareStatement(query);

            int index = 1;
            ps.setInt(index++, message.getUserId());
            ps.setBoolean(index++, message.isProcessed());
            ps.setTimestamp(index++, TimeUtils.convertDateToTimestamp(message.getToTime()));
            ps.setInt(index++, message.getProcessId());
            ps.setInt(index++, message.getId());
            ps.executeUpdate();

            ps.close();

            updateProcessLastMessageTime(message);
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }
    
    public void updateMessageTags(int messageId, Set<Integer> tagIds) throws Exception {
        String query = SQL_DELETE + TABLE_MESSAGE_TAG + SQL_WHERE + "message_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, messageId);
        ps.executeUpdate();
        ps.close();

        query = SQL_INSERT + TABLE_MESSAGE_TAG + "(message_id, tag_id) VALUES (?,?)";
        ps = con.prepareStatement(query);
        ps.setInt(1, messageId);
        for (int tagId : tagIds) {
            ps.setInt(2, tagId);
            ps.executeUpdate();
        }   
        ps.close();
    }

    public Set<Integer> getMessageTags(int messageId) throws Exception {
        return getIds(TABLE_MESSAGE_TAG, "message_id", "tag_id", messageId);
    }

    public void deleteMessage(int id) throws BGException {
        try {
            Message message = null;
            if (id > 0) {
                message = getMessageById(id);
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM " + TABLE_MESSAGE + "WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement(SQL_DELETE + TABLE_MESSAGE_TAG + SQL_WHERE + "message_id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

            if (message != null) {
                updateProcessLastMessageTime(message);
            }
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }

    public void deleteProcessMessages(int processId) {
        // TODO: Delete using join from: TABLE_MESSAGE, TABLE_MESSAGE_TAG, TABLE_PROCESS_MESSAGE_STATE
    }

    private void updateProcessLastMessageTime(Message message) throws BGException {
        try {
            if (message.getProcessId() > 0) {
                // пока только для входящих
                //if( message.getDirection() == Message.DIRECTION_INCOMING )
                {
                    Date lastIncomingTime = null;
                    int countIn = 0, countInUnread = 0, lastInId = 0, countOut = 0, lastOutId = 0;

                    String query = "SELECT CAST(MAX(IF(direction=1,from_dt,NULL)) AS DATETIME), COUNT(IF(direction=1,1,0)), SUM(IF(direction=1 AND ISNULL(to_dt), 1, 0)), MAX(IF(direction=1,id,0)), "
                            + "COUNT(IF(direction=2,1,NULL)), MAX(IF(direction=2,id,0)) " + "FROM " + TABLE_MESSAGE
                            + "WHERE process_id=?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setInt(1, message.getProcessId());

                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        lastIncomingTime = TimeUtils.convertTimestampToDate(rs.getTimestamp(1));
                        countIn = rs.getInt(2);
                        countInUnread = rs.getInt(3);
                        lastInId = rs.getInt(4);
                        countOut = rs.getInt(5);
                        lastOutId = rs.getInt(6);
                    }
                    ps.close();

                    int pos = 0;
                    query = "INSERT INTO " + TABLE_PROCESS_MESSAGE_STATE + " SET " + "process_id=?, "
                            + "in_last_dt=?, in_count=?, in_unread_count=?, in_last_id=?, out_count=?, out_last_id=? "
                            + SQL_ON_DUP_KEY_UPDATE
                            + "in_last_dt=?, in_count=?, in_unread_count=?, in_last_id=?, out_count=?, out_last_id=?";
                    ps = con.prepareStatement(query);
                    ps.setInt(++pos, message.getProcessId());
                    for (int i = 0; i < 2; i++) {
                        ps.setTimestamp(++pos, TimeUtils.convertDateToTimestamp(lastIncomingTime));
                        ps.setInt(++pos, countIn);
                        ps.setInt(++pos, countInUnread);
                        ps.setInt(++pos, lastInId);
                        ps.setInt(++pos, countOut);
                        ps.setInt(++pos, lastOutId);
                    }
                    ps.executeUpdate();
                    ps.close();
                }
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<Message> getUnsendMessageList(int type, int maxCount) throws BGException {
        List<Message> result = new ArrayList<Message>();

        try {
            String query = "SELECT * FROM " + TABLE_MESSAGE + "WHERE type_id=? AND direction=? AND to_dt IS NULL "
                    + "LIMIT ?";

            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, type);
            ps.setInt(2, Message.DIRECTION_OUTGOING);
            ps.setInt(3, maxCount);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(getMessageFromRs(rs));
            }
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }

        return result;
    }

    public void searchMessageList(SearchResult<Message> searchResult, Integer processId, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from)
            throws BGException {
        searchMessageList(searchResult, processId, typeId, direction, processed, withAttach, 
                dateFrom, dateTo, from, true);
    }
    
    public void searchMessageList(SearchResult<Message> searchResult, Integer processId, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder) throws BGException {
        searchMessageList(searchResult, processId, typeId, direction, processed, withAttach, 
                dateFrom, dateTo, from, true, null);
    }

    public void searchMessageList(SearchResult<Message> searchResult, Integer processId, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder, Set<Integer> tagIds) throws BGException {
        searchMessageList(searchResult, processId != null ? Collections.singleton(processId) : null, typeId != null ? Collections.singleton(typeId) : null,
                direction, processed, withAttach, dateFrom, dateTo, from, true, tagIds);
    }

    @Deprecated
    public void searchMessageList(SearchResult<Message> searchResult, Collection<Integer> processIds, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder, Set<Integer> tagIds) throws BGException {
        searchMessageList(searchResult, processIds, typeId != null ? Collections.singleton(typeId) : null, direction, processed, withAttach, 
                dateFrom, dateTo, from, true, tagIds);
    }
    
    public void searchMessageList(SearchResult<Message> searchResult, Collection<Integer> processIds, Set<Integer> typeIds,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder, Set<Integer> tagIds) throws BGException {
        try {
            Page page = searchResult.getPage();

            PreparedDelay ps = new PreparedDelay(con);
            ps.addQuery(SQL_SELECT_COUNT_ROWS + " m.*, p.* FROM " + TABLE_MESSAGE + " AS m " 
                    + "LEFT JOIN " + TABLE_PROCESS + " AS p ON m.process_id=p.id ");
            if (CollectionUtils.isNotEmpty(tagIds))
                ps.addQuery(SQL_INNER_JOIN + TABLE_MESSAGE_TAG + " AS mt ON m.id=mt.message_id AND mt.tag_id IN (" + Utils.toString(tagIds) + ")");
            ps.addQuery("WHERE 1>0 ");
            if (processIds != null) {
                ps.addQuery(" AND m.process_id IN (");
                ps.addQuery(Utils.toString(processIds));
                ps.addQuery(")");
            }
            if (CollectionUtils.isNotEmpty(typeIds)) {
                ps.addQuery(" AND m.type_id IN (");
                ps.addQuery(Utils.toString(typeIds));
                ps.addQuery(")");
            }
            if (direction != null) {
                ps.addQuery(" AND m.direction=?");
                ps.addInt(direction);
            }
            if (processed != null) {
                if (processed) {
                    ps.addQuery(" AND processed");
                } else {
                    ps.addQuery(" AND NOT(processed)");
                }
            }
            if (withAttach != null) {
                if (withAttach) {
                    ps.addQuery(" AND attach_data");
                } else {
                    ps.addQuery(" AND NOT(attach_data)");
                }
            }

            if (dateFrom != null) {
                ps.addQuery(" AND ?< m.from_dt");
                ps.addDate(dateFrom);

            }
            if (dateTo != null) {
                ps.addQuery(" AND m.from_dt <?");
                ps.addDate(TimeUtils.getNextDay(dateTo));

            }
            if (Utils.notBlankString(from)) {
                ps.addQuery(" AND m.from LIKE ?");
                ps.addString(from);
            }
            ps.addQuery(" ORDER BY m.from_dt ");
            if (reverseOrder) {
                ps.addQuery(" DESC");
            }
            ps.addQuery(getPageLimit(page));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Message message = getMessageFromRs(rs, "m.");
                searchResult.getList().add(message);

                if (rs.getInt("p.id") > 0) {
                    message.setProcess(ProcessDAO.getProcessFromRs(rs, "p."));
                }
            }

            setRecordCount(page, ps.getPrepared());
            ps.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }

    /**
     * Retrieves process message list, sorted by ID.
     * @param processId process ID.
     * @param beforeMessageId if &gt; 0 - filter from message ID.
     * @return
     * @throws Exception
     */
    public List<Message> getProcessMessageList(int processId, int beforeMessageId) throws Exception {
        List<Message> list = new ArrayList<Message>();

        var pd = new PreparedDelay(con, 
            SQL_SELECT_COUNT_ROWS + " * FROM " + TABLE_MESSAGE +  "WHERE process_id=?");
        pd.addInt(processId);
        if (beforeMessageId > 0) {
            pd.addQuery(" AND id<?");
            pd.addInt(beforeMessageId);
        }
        pd.addQuery(" ORDER BY id");
        
        var rs = pd.executeQuery();
        while (rs.next())
            list.add(getMessageFromRs(rs));
        
        pd.close();

        return list;
    }
    
    public Map<Integer, Set<Integer>> getProcessMessageTagMap(int processId) throws Exception {
        return getProcessMessageTagMap(Collections.singleton(processId));
    }

    public Map<Integer, Set<Integer>> getProcessMessageTagMap(Collection<Integer> processIds) throws Exception {
        Map<Integer, Set<Integer>> result = new HashMap<>();
        
        String query = "SELECT m.id, m.attach_data, mt.tag_id FROM " + TABLE_MESSAGE + " AS m "
                + SQL_LEFT_JOIN + TABLE_MESSAGE_TAG + " AS mt ON m.id=mt.message_id "
                + SQL_WHERE + "m.process_id IN (" + Utils.toString(processIds) + ")";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Set<Integer> messageTags = null;
                if (!Utils.isBlankString(rs.getString(2))) {
                    messageTags = result.computeIfAbsent(rs.getInt(1), id -> new HashSet<>());
                    messageTags.add(TagConfig.Tag.TAG_ATTACH_ID);
                }
                int tagId = rs.getInt(3);
                if (tagId > 0) {
                    if (messageTags == null)
                        messageTags = result.computeIfAbsent(rs.getInt(1), id -> new HashSet<>());
                    messageTags.add(tagId);
                }
            }
        }
        
        return result;
    }

    /**
     * Searches messages in processes.
     * @param processIds process IDs.
     * @param text message substring.
     * @return
     * @throws Exception
     */
    public List<Message> getProcessMessageList(Set<Integer> processIds, String text) throws Exception {
        var result = new ArrayList<Message>();
        if (processIds.isEmpty() || Utils.isBlankString(text))
            return result;
        
        var query = new StringBuilder(200);
        query.append(SQL_SELECT + "message.*, p.description " + SQL_FROM + TABLE_MESSAGE + " AS message ");
        query.append(getIsolationJoin(user));
        query.append(SQL_LEFT_JOIN + TABLE_PROCESS + "AS p ON message.process_id=p.id");
        query.append(SQL_WHERE + "message.process_id IN (");
        query.append(Utils.toString(processIds));
        query.append(") AND message.text LIKE ?");

        try (var ps = con.prepareStatement(query.toString())) {
            ps.setString(1, getLikePatternSub(text));

            var rs = ps.executeQuery();
            while (rs.next()) {
                var m = getMessageFromRs(rs, "message.");
                var p = new Process(m.getProcessId());
                p.setDescription(rs.getString("p.description"));
                m.setProcess(p);
                result.add(m);
            }
        }

        return result;
    }

    public static String getIsolationJoin(User user) {
        var isolation = user.getConfigMap().getConfig(IsolationConfig.class);
        if (isolation.getIsolationProcess() != null) {
            return
                SQL_INNER_JOIN + TABLE_PROCESS + " ON message.process_id=process.id " +
                ProcessDAO.getIsolationJoin(user);
        }
        return "";
    }

    private Message getMessageFromRs(ResultSet rs) throws SQLException {
        return getMessageFromRs(rs, "");
    }

    public static Message getMessageFromRs(ResultSet rs, String prefix) throws SQLException {
        Message result = new Message();

        result.setId(rs.getInt(prefix + "id"));
        result.setSystemId(rs.getString(prefix + "system_id"));
        result.setTypeId(rs.getInt(prefix + "type_id"));
        result.setProcessId(rs.getInt(prefix + "process_id"));
        result.setDirection(rs.getInt(prefix + "direction"));
        result.setFrom(rs.getString(prefix + "from"));
        result.setTo(rs.getString(prefix + "to"));
        result.setSubject(rs.getString(prefix + "subject"));
        result.setText(rs.getString(prefix + "text"));
        result.setFromTime(TimeUtils.convertTimestampToDate(rs.getTimestamp(prefix + "from_dt")));
        result.setToTime(TimeUtils.convertTimestampToDate(rs.getTimestamp(prefix + "to_dt")));
        result.setUserId(rs.getInt(prefix + "user_id"));
        result.setProcessed(rs.getBoolean(prefix + "processed"));
        result.setSystemId(rs.getString(prefix + "system_id"));
        for (FileData data : FileData.parse(rs.getString(prefix + "attach_data"))) {
            result.addAttach(data);
        }

        return result;
    }
}