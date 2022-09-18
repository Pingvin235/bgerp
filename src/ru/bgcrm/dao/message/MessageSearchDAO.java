package ru.bgcrm.dao.message;

import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE_TAG;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Period;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Fluent DAO for message search.
 *
 * @author Shamil Vakhitov
 */
public class MessageSearchDAO extends MessageDAO {
    private Set<Integer> tagIds;
    private Set<Integer> processIds;
    private Set<Integer> typeIds;
    private Integer direction;
    private Boolean processed;
    private Boolean attach;
    private Period dateFrom;
    private String from;
    private boolean fromTimeReverseOrder;

    public MessageSearchDAO(Connection con) {
       super(con);
    }

    /**
     * Filter by message tag IDs.
     * @param value
     * @return
     */
    public MessageSearchDAO withTagIds(Set<Integer> value) {
        this.tagIds = value;
        return this;
    }

    /**
     * Filter by message tag ID.
     * @param value positive ID.
     * @return
     */
    public MessageSearchDAO withTagId(int value) {
        if (value > 0)
            this.tagIds = Collections.singleton(value);
        return this;
    }

    /**
     * Filter by linked to message process IDs.
     * @param value
     * @return
     */
    public MessageSearchDAO withProcessIds(Set<Integer> value) {
        this.processIds = value;
        return this;
    }

    /**
     * Filter by message type IDs.
     * @param value
     * @return
     */
    public MessageSearchDAO withTypeIds(Set<Integer> value) {
        this.typeIds = value;
        return this;
    }

    /**
     * Filter by single type ID.
     * @param value positive ID.
     * @return
     */
    public MessageSearchDAO withTypeId(int value) {
        if (value > 0)
            this.typeIds = Collections.singleton(value);
        return this;
    }

    /**
     * Filter by {@link Message#getDirection()}.
     * @param value
     * @return
     */
    public MessageSearchDAO withDirection(Integer value) {
        this.direction = value;
        return this;
    }

    /**
     * Filter by {@link Message#isProcessed()}.
     * @param value
     * @return
     */
    public MessageSearchDAO withProcessed(Boolean value) {
        this.processed = value;
        return this;
    }

    /**
     * Filter by attachment existence.
     * @param value
     * @return
     */
    public MessageSearchDAO withAttach(Boolean value) {
        this.attach = value;
        return this;
    }

    /**
     * SQL LIKE filter by {@link Message#getFrom()}.
     * @param value
     * @return
     */
    public MessageSearchDAO withFrom(String value) {
        this.from = value;
        return this;
    }

    /**
     * Filter by {@link Message#getFromTime()}, day precision.
     * @param from from day including.
     * @param to to day, including.
     * @return
     */
    public MessageSearchDAO withDateFrom(Date from, Date to) {
        this.dateFrom = new Period(from, to);
        return this;
    }

    /**
     * Sorting order reversed {@link Message#getFromTime()}.
     * @param value
     * @return
     */
    public MessageSearchDAO withFromTimeReverseOrder(boolean value) {
        this.fromTimeReverseOrder = true;
        return this;
    }

    public void search(Pageable<Message> result) throws SQLException {
        var page = result.getPage();

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
                ps.addQuery(" AND process_id!=0");
            } else {
                ps.addQuery(" AND process_id=0");
            }
        }
        if (attach != null) {
            if (attach) {
                ps.addQuery(" AND attach_data");
            } else {
                ps.addQuery(" AND NOT(attach_data)");
            }
        }

        if (dateFrom != null) {
            if (dateFrom.getDateFrom() != null) {
                ps.addQuery(" AND ?<m.from_dt");
                ps.addDate(dateFrom.getDateFrom());
            }
            if (dateFrom.getDateTo() != null) {
                ps.addQuery(" AND m.from_dt<?");
                ps.addDate(TimeUtils.getNextDay(dateFrom.getDateTo()));
            }
        }

        if (Utils.notBlankString(from)) {
            ps.addQuery(" AND m.from LIKE ?");
            ps.addString(from);
        }
        ps.addQuery(" ORDER BY m.from_dt ");
        if (fromTimeReverseOrder) {
            ps.addQuery(" DESC");
        }
        ps.addQuery(getPageLimit(page));

        var rs = ps.executeQuery();
        while (rs.next()) {
            Message message = getMessageFromRs(rs, "m.");
            result.getList().add(message);

            if (rs.getInt("p.id") > 0) {
                message.setProcess(ProcessDAO.getProcessFromRs(rs, "p."));
            }
        }

        setRecordCount(page, ps.getPrepared());
        ps.close();
    }
}
