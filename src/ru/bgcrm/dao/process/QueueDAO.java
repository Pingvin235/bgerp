package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS_TITLE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_TYPE;
import static ru.bgcrm.dao.process.Tables.TABLE_QUEUE;
import static ru.bgcrm.dao.process.Tables.TABLE_QUEUE_PROCESS_TYPE;
import static ru.bgcrm.dao.user.Tables.TABLE_USER;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.model.Pageable;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.dao.LastModifyDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.queue.QueueProcessStat;
import ru.bgcrm.model.process.queue.QueueStat;
import ru.bgcrm.model.process.queue.QueueUserStat;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class QueueDAO extends CommonDAO {
    public QueueDAO(Connection con) {
        super(con);
    }

    public void searchQueue(Pageable<Queue> searchResult, Set<Integer> queueIds, String filter) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Queue> list = searchResult.getList();

            ResultSet rs = null;
            PreparedStatement ps = null;

            StringBuilder query = new StringBuilder();
            query.append(" SELECT SQL_CALC_FOUND_ROWS * FROM queue ");
            query.append(" WHERE 1=1 ");
            if (CollectionUtils.isNotEmpty(queueIds)) {
                query.append(" AND id IN (" + Utils.toString(queueIds) + ") ");
            }
            if (Utils.notEmptyString(filter)) {
                query.append(" AND (title LIKE '%" + filter + "%' OR config LIKE '%" + filter + "%')");
            }
            query.append(" ORDER BY title");
            query.append(getPageLimit(page));

            ps = con.prepareStatement(query.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(getQueueFromRs(rs));
            }
            page.setRecordCount(foundRows(ps));
            ps.close();
        }
    }

    public List<Queue> getQueueList() throws SQLException {
        List<Queue> result = new ArrayList<Queue>();

        ResultSet rs = null;
        PreparedStatement ps = null;

        ps = con.prepareStatement("SELECT * FROM queue ORDER BY title");
        rs = ps.executeQuery();
        while (rs.next()) {
            result.add(getQueueFromRs(rs));
        }
        ps.close();

        return result;
    }

    public Queue getQueue(int id) throws SQLException {
        Queue result = null;

        ResultSet rs = null;
        PreparedStatement ps = null;

        ps = con.prepareStatement("SELECT * FROM queue WHERE id=?");
        ps.setInt(1, id);
        rs = ps.executeQuery();
        if (rs.next()) {
            result = getQueueFromRs(rs);
            result.setLastModify(LastModifyDAO.getLastModify(rs));
        }
        ps.close();

        return result;
    }

    /**
     * Updates process queue entity.
     * @param queue entity data, for insertion {@link Queue#getId()} &lt;= 0.
     * @param userId user ID for checking conflicting updates.
     * @throws BGException
     * @throws SQLException
     */
    public void updateQueue(Queue queue, int userId) throws BGException, SQLException {
        int index = 1;
        PreparedStatement ps = null;

        if (queue.getId() <= 0) {
            //TODO: добавить валидацию конфигурациии
            ps = con.prepareStatement("INSERT INTO queue SET title=?, config=?, " + LastModifyDAO.LAST_MODIFY_COLUMNS,
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(index++, queue.getTitle());
            ps.setString(index++, queue.getConfig());
            LastModifyDAO.setLastModifyFields(ps, index++, index++, new LastModify(userId, new Date()));
            ps.executeUpdate();
            queue.setId(lastInsertId(ps));
        } else {
            ps = con.prepareStatement("UPDATE queue SET title=?, config=?, " + LastModifyDAO.LAST_MODIFY_COLUMNS + " WHERE id=?");
            ps.setString(index++, queue.getTitle());
            ps.setString(index++, queue.getConfig());
            LastModifyDAO.setLastModifyFields(ps, index++, index++, queue.getLastModify());
            ps.setInt(index++, queue.getId());
            ps.executeUpdate();
        }
        ps.close();

        Preferences.processIncludes(new ConfigDAO(con), queue.getConfig(), true);

        if (queue.getProcessTypeIds() != null) {
            updateIds(TABLE_QUEUE_PROCESS_TYPE, "queue_id", "type_id", queue.getId(), queue.getProcessTypeIds());
        }
    }

    public Set<Integer> getQueueProcessTypeIds(int id) throws SQLException {
        return getIds(TABLE_QUEUE_PROCESS_TYPE, "queue_id", "type_id", id);
    }

    private Queue getQueueFromRs(ResultSet rs) throws SQLException {
        Queue result = new Queue();

        result.setId(rs.getInt("id"));
        result.setTitle(rs.getString("title"));
        result.setConfig(rs.getString("config"));

        return result;
    }

    public QueueStat getQueueStat(Queue queue, String userIds) throws SQLException {
        QueueStat stat = new QueueStat();

        stat.setQueueTitle(queue.getTitle());
        stat.setProcessStat(getQueueProcessStat(queue));
        stat.setUserStat(getQueueUserStat(queue, userIds));

        return stat;
    }

    public List<QueueProcessStat> getQueueProcessStat(Queue queue) throws SQLException {
        List<QueueProcessStat> result = new ArrayList<QueueProcessStat>();

        if (queue.getProcessTypeIds().size() > 0) {

            StringBuilder query = new StringBuilder();
            query.append(SQL_SELECT);
            query.append("type.title, status.title, COUNT(*), process.type_id, process.status_id");
            query.append(SQL_FROM);
            query.append(TABLE_PROCESS);
            query.append("AS process");
            query.append(SQL_LEFT_JOIN);
            query.append(TABLE_PROCESS_TYPE);
            query.append("AS type ON process.type_id=type.id");
            query.append(SQL_LEFT_JOIN);
            query.append(TABLE_PROCESS_STATUS_TITLE);
            query.append("AS status ON process.status_id=status.id");
            query.append(SQL_WHERE);
            query.append("process.type_id IN(");
            query.append(Utils.toString(queue.getProcessTypeIds()));
            query.append(") AND process.close_dt IS NULL");
            query.append(SQL_GROUP_BY);
            query.append("process.type_id, process.status_id");
            query.append(SQL_ORDER_BY);
            query.append("type.title, status.title");

            PreparedStatement ps = con.prepareStatement(query.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QueueProcessStat stat = new QueueProcessStat();
                stat.setTypeTitle(rs.getString(1));
                stat.setStatusTitle(rs.getString(2));
                stat.setProcessCount(rs.getInt(3));
                result.add(stat);
            }
            ps.close();
        }

        return result;
    }

    public List<QueueUserStat> getQueueUserStat(Queue queue, String userIds) throws SQLException {
        List<QueueUserStat> result = new ArrayList<QueueUserStat>();

        StringBuilder query = new StringBuilder();
        query.append(SQL_SELECT);
        query.append("user.title, COUNT(process.id), user.id");
        query.append(SQL_FROM);
        query.append(TABLE_USER);
        query.append("AS user");
        query.append(SQL_LEFT_JOIN);
        query.append(Tables.TABLE_PROCESS_EXECUTOR);
        query.append("AS executor ON user.id=executor.user_id");
        query.append(SQL_LEFT_JOIN);
        query.append(TABLE_PROCESS);
        query.append("AS process ON process.id=executor.process_id AND process.type_id IN(");
        query.append(Utils.toString(queue.getProcessTypeIds()));
        query.append(") AND process.close_dt IS NULL");
        query.append(SQL_WHERE);
        query.append("user.id IN (");
        query.append(userIds);
        query.append(")");
        query.append(SQL_GROUP_BY);
        query.append("user.id");
        query.append(SQL_ORDER_BY);
        query.append("user.title");

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            QueueUserStat stat = new QueueUserStat();
            stat.setUserTitle(rs.getString(1));
            stat.setProcessCount(rs.getInt(2));
            result.add(stat);
        }
        ps.close();

        return result;
    }

    public void delete(int id) throws SQLException {
        PreparedStatement ps = null;

        StringBuilder query = new StringBuilder();
        query.append(SQL_DELETE);
        query.append(TABLE_QUEUE);
        query.append(SQL_WHERE);
        query.append("id=?");

        ps = con.prepareStatement(query.toString());
        ps.setInt(1, id);
        ps.executeUpdate();

        ps.close();
    }
}
