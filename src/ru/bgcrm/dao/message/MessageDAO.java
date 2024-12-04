package ru.bgcrm.dao.message;

import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE_TAG;
import static ru.bgcrm.dao.message.Tables.TABLE_PROCESS_MESSAGE_STATE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import org.bgerp.app.exception.BGException;
import org.bgerp.dao.message.MessageSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.config.IsolationConfig;
import org.bgerp.util.TimeConvert;
import org.bgerp.util.sql.LikePattern;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.message.TagConfig;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Message DAO.
 *
 * @author Shamil Vakhitov
 */
public class MessageDAO extends CommonDAO {
    private final DynActionForm form;

    public MessageDAO(Connection con) {
        super(con);
        this.form = null;
    }

    public MessageDAO(Connection con, DynActionForm form) {
        super(con);
        this.form = form;
    }

    /**
     * Selects a message by ID.
     * @param id
     * @return
     * @throws SQLException
     */
    public Message getMessageById(int id) throws SQLException {
        Message result = null;

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

        return result;
    }

    /**
     * Selects a message by system ID.
     * @param typeId
     * @param systemId
     * @return
     * @throws SQLException
     */
    public Message getMessageBySystemId(int typeId, String systemId) throws SQLException {
        Message result = null;

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

        return result;
    }

    /**
     * Updates message entity.
     * @param message
     * @throws SQLException
     */
    public void updateMessage(Message message) throws SQLException {
        PreparedStatement ps = null;

        if (message.getId() <= 0) {
            String query = "INSERT INTO " + TABLE_MESSAGE
                    + "(system_id, type_id, direction, `from`, `to`, subject, text, user_id, process_id, from_dt, to_dt, attach_data) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
            String query = "UPDATE " + TABLE_MESSAGE
                    + "SET system_id=?, type_id=?, direction=?, `from`=?, `to`=?, subject=?, text=?, user_id=?, process_id=?, from_dt=?, to_dt=?, attach_data=? "
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
        ps.setInt(index++, message.getProcessId());
        ps.setTimestamp(index++, TimeConvert.toTimestamp(message.getFromTime()));
        ps.setTimestamp(index++, TimeConvert.toTimestamp(message.getToTime()));
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
    }

    /**
     * Marks a message as processed (related to process).
     * @param message
     * @throws SQLException
     */
    public void updateMessageProcess(Message message) throws SQLException {
        String query = "UPDATE " + TABLE_MESSAGE + "SET user_id=?, to_dt=?, process_id=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(query);

        int index = 1;
        ps.setInt(index++, message.getUserId());
        ps.setTimestamp(index++, TimeConvert.toTimestamp(message.getToTime()));
        ps.setInt(index++, message.getProcessId());
        ps.setInt(index++, message.getId());
        ps.executeUpdate();

        ps.close();

        updateProcessLastMessageTime(message);
    }

    /**
     * Updates message tags
     * @param messageId the message ID
     * @param tagIds the tag IDs
     * @param positiveOnly only positive tags will be deleted before insertion
     * @throws SQLException
     */
    public void updateMessageTags(int messageId, Set<Integer> tagIds, boolean positiveOnly) throws SQLException {
        String query = SQL_DELETE_FROM + TABLE_MESSAGE_TAG + SQL_WHERE + "message_id=?";
        if (positiveOnly)
            query += SQL_AND + "tag_id>0";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
        }

        query = SQL_INSERT_INTO + TABLE_MESSAGE_TAG + "(message_id, tag_id) VALUES (?,?)";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, messageId);
            for (int tagId : tagIds) {
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Adds or removes message tags
     * @param messageId the message ID
     * @param tagIds the tag IDs
     * @param add add or delete
     * @throws SQLException
     */
    public void toggleMessageTags(int messageId, Set<Integer> tagIds, boolean add) throws SQLException {
        if (add) {
            String query = SQL_INSERT_INTO + TABLE_MESSAGE_TAG + "(message_id, tag_id)" + SQL_VALUES + "(?,?)";
            try (var ps = con.prepareStatement(query)) {
                ps.setInt(1, messageId);
                for (int tagId : tagIds) {
                    ps.setInt(2, tagId);
                    ps.executeUpdate();
                }
            }
        } else if (!tagIds.isEmpty()) {
            String query = SQL_DELETE_FROM + TABLE_MESSAGE_TAG + SQL_WHERE + "message_id=?" + SQL_AND + "tag_id IN (" + Utils.toString(tagIds) + ")";
            try (var ps = con.prepareStatement(query)) {
                ps.setInt(1, messageId);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Selects message tags.
     * @param messageId
     * @return
     * @throws SQLException
     */
    public Set<Integer> getMessageTags(int messageId) throws SQLException {
        return getIds(TABLE_MESSAGE_TAG, "message_id", "tag_id", messageId);
    }

    /**
     * Deletes all message related entities and attached files.
     * @param id the message ID.
     * @throws SQLException
     */
    public void deleteMessage(int id) throws Exception {
        Message message = null;
        if (id > 0) {
            message = getMessageById(id);
        }

        PreparedStatement ps = con.prepareStatement("DELETE FROM " + TABLE_MESSAGE + "WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();

        ps = con.prepareStatement(SQL_DELETE_FROM + TABLE_MESSAGE_TAG + SQL_WHERE + "message_id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();

        if (message != null) {
            updateProcessLastMessageTime(message);

            List<FileData> attaches = message.getAttachList();
            if (attaches != null) {
                var dao = new FileDataDAO(con);
                for (var file : attaches)
                    dao.delete(file);
            }
        }
    }

    /**
     * Delete process related messages and all related to them entities.
     * @param processId process ID.
     * @throws SQLException
     */
    public void deleteProcessMessages(int processId) throws SQLException {
        String query = "DELETE message, process_message_state, message_tag" + SQL_FROM + TABLE_MESSAGE + "AS message"
            + SQL_INNER_JOIN + TABLE_PROCESS_MESSAGE_STATE + "AS process_message_state ON message.process_id=process_message_state.process_id"
            + SQL_INNER_JOIN + TABLE_MESSAGE_TAG + "AS message_tag ON message.id=message_tag.message_id"
            + SQL_WHERE + "message.process_id=?";
        try (var ps = con.prepareStatement(query)) {
            ps.setInt(1, processId);
            ps.executeUpdate();
        }
    }

    /**
     * Updates process message statistic in {@link Tables#TABLE_PROCESS_MESSAGE_STATE}.
     * @param message message with {@link Message#getProcessId()} &gt; 0
     * @throws SQLException
     */
    private void updateProcessLastMessageTime(Message message) throws SQLException {
        if (message.getProcessId() > 0) {
            Timestamp lastIncomingTime = null;
            int countIn = 0, countInUnread = 0, lastInId = 0, countOut = 0, lastOutId = 0;

            String query = "SELECT CAST(MAX(IF(direction=1,from_dt,NULL)) AS DATETIME), COUNT(IF(direction=1,1,0)), SUM(IF(direction=1 AND ISNULL(to_dt), 1, 0)), MAX(IF(direction=1,id,0)), "
                    + "COUNT(IF(direction=2,1,NULL)), MAX(IF(direction=2,id,0)) " + "FROM " + TABLE_MESSAGE
                    + "WHERE process_id=?";
            try (var ps = con.prepareStatement(query)) {
                ps.setInt(1, message.getProcessId());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    lastIncomingTime = rs.getTimestamp(1);
                    countIn = rs.getInt(2);
                    countInUnread = rs.getInt(3);
                    lastInId = rs.getInt(4);
                    countOut = rs.getInt(5);
                    lastOutId = rs.getInt(6);
                }
            }

            int pos = 0;
            query = "INSERT INTO " + TABLE_PROCESS_MESSAGE_STATE + " SET " + "process_id=?, "
                    + "in_last_dt=?, in_count=?, in_unread_count=?, in_last_id=?, out_count=?, out_last_id=? "
                    + SQL_ON_DUP_KEY_UPDATE
                    + "in_last_dt=?, in_count=?, in_unread_count=?, in_last_id=?, out_count=?, out_last_id=?";
            try (var ps = con.prepareStatement(query)) {
                ps.setInt(++pos, message.getProcessId());
                for (int i = 0; i < 2; i++) {
                    ps.setTimestamp(++pos, lastIncomingTime);
                    ps.setInt(++pos, countIn);
                    ps.setInt(++pos, countInUnread);
                    ps.setInt(++pos, lastInId);
                    ps.setInt(++pos, countOut);
                    ps.setInt(++pos, lastOutId);
                }
                ps.executeUpdate();
            }
        }
    }

    public List<Message> getUnsendMessageList(int type, int maxCount) {
        List<Message> result = new ArrayList<>();

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

    /** Use {@link MessageSearchDAO}. */
    @Deprecated
    public void searchMessageList(Pageable<Message> searchResult, Integer processId, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from)
            {
        searchMessageList(searchResult, processId, typeId, direction, processed, withAttach,
                dateFrom, dateTo, from, true);
    }

    /** Use {@link MessageSearchDAO}. */
    @Deprecated
    public void searchMessageList(Pageable<Message> searchResult, Integer processId, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder) {
        searchMessageList(searchResult, processId, typeId, direction, processed, withAttach,
                dateFrom, dateTo, from, true, null);
    }

    /** Use {@link MessageSearchDAO}. */
    @Deprecated
    public void searchMessageList(Pageable<Message> searchResult, Integer processId, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder, Set<Integer> tagIds) {
        searchMessageList(searchResult, processId != null ? Collections.singleton(processId) : null, typeId != null ? Collections.singleton(typeId) : null,
                direction, processed, withAttach, dateFrom, dateTo, from, true, tagIds);
    }

    /** Use {@link MessageSearchDAO}. */
    @Deprecated
    public void searchMessageList(Pageable<Message> searchResult, Collection<Integer> processIds, Integer typeId,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder, Set<Integer> tagIds) {
        searchMessageList(searchResult, processIds, typeId != null ? Collections.singleton(typeId) : null, direction, processed, withAttach,
                dateFrom, dateTo, from, true, tagIds);
    }

    /** Use {@link MessageSearchDAO}. */
    @Deprecated
    public void searchMessageList(Pageable<Message> searchResult, Collection<Integer> processIds, Set<Integer> typeIds,
            Integer direction, Boolean processed, Boolean withAttach, Date dateFrom, Date dateTo, String from,
            boolean reverseOrder, Set<Integer> tagIds) {
        try {
            Page page = searchResult.getPage();

            PreparedQuery ps = new PreparedQuery(con);
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
        List<Message> list = new ArrayList<>();

        var pq = new PreparedQuery(con,
            SQL_SELECT_COUNT_ROWS + " * FROM " + TABLE_MESSAGE +  "WHERE process_id=?");
        pq.addInt(processId);
        if (beforeMessageId > 0) {
            pq.addQuery(" AND id<?");
            pq.addInt(beforeMessageId);
        }
        pq.addQuery(" ORDER BY id");

        var rs = pq.executeQuery();
        while (rs.next())
            list.add(getMessageFromRs(rs));

        pq.close();

        return list;
    }

    public Map<Integer, Set<Integer>> getProcessMessageTagMap(int processId) throws SQLException {
        return getProcessMessageTagMap(Collections.singleton(processId));
    }

    public Map<Integer, Set<Integer>> getProcessMessageTagMap(Collection<Integer> processIds) throws SQLException {
        Map<Integer, Set<Integer>> result = new HashMap<>();

        String query = SQL_SELECT + "m.id, m.to_dt, m.attach_data, mt.tag_id" + SQL_FROM + TABLE_MESSAGE + "AS m"
                + SQL_LEFT_JOIN + TABLE_MESSAGE_TAG + " AS mt ON m.id=mt.message_id "
                + SQL_WHERE + "m.process_id IN (" + Utils.toString(processIds) + ")";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Set<Integer> messageTags = null;

                int messageId = rs.getInt("m.id");
                if (!Utils.isBlankString(rs.getString("m.attach_data"))) {
                    messageTags = result.computeIfAbsent(messageId, id -> new HashSet<>());
                    messageTags.add(TagConfig.Tag.TAG_ATTACH_ID);
                }

                if (rs.getTimestamp("m.to_dt") == null) {
                    messageTags = result.computeIfAbsent(messageId, id -> new HashSet<>());
                    messageTags.add(TagConfig.Tag.TAG_UNREAD_ID);
                }

                int tagId = rs.getInt("mt.tag_id");
                if (messageTags == null)
                    messageTags = result.computeIfAbsent(messageId, id -> new HashSet<>());

                messageTags.add(tagId);
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
        query.append(getIsolationJoin(form));
        query.append(SQL_LEFT_JOIN + TABLE_PROCESS + "AS p ON message.process_id=p.id");
        query.append(SQL_WHERE + "message.process_id IN (");
        query.append(Utils.toString(processIds));
        query.append(") AND message.text LIKE ?");

        try (var ps = con.prepareStatement(query.toString())) {
            ps.setString(1, LikePattern.SUB.get(text));

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

    public static String getIsolationJoin(DynActionForm form) {
        if (form == null)
            return "";

        User user = form.getUser();

        var isolation = user.getConfigMap().getConfig(IsolationConfig.class);
        if (isolation.getIsolationProcess() != null) {
            return
                SQL_INNER_JOIN + TABLE_PROCESS + " AS process ON message.process_id=process.id " +
                ProcessDAO.getIsolationJoin(form, "process");
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
        result.setFromTime(rs.getTimestamp(prefix + "from_dt"));
        result.setToTime(rs.getTimestamp(prefix + "to_dt"));
        result.setUserId(rs.getInt(prefix + "user_id"));
        result.setSystemId(rs.getString(prefix + "system_id"));
        for (FileData data : FileData.parse(rs.getString(prefix + "attach_data"))) {
            result.addAttach(data);
        }

        return result;
    }
}