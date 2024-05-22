package org.bgerp.dao.message;

import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE_TAG;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.bgerp.model.Pageable;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Period;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.message.TagConfig;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Fluent DAO for message search.
 *
 * @author Shamil Vakhitov
 */
public class MessageSearchDAO extends MessageDAO {
    public static enum Order {
        FROM_TIME,
        FROM_TIME_DESC,
        PINNED_FIRST
    }


    private Set<Integer> tagIds;
    private Set<Integer> processIds;
    private Set<Integer> typeIds;
    private Integer direction;
    private Boolean processed;
    private Boolean read;
    private Boolean attach;
    private Period dateFrom;
    private String from;
    private List<Order> orders = new ArrayList<>();

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
     * Filter by {@link Message#getToTime()} not {@code null}.
     * @param value
     * @return
     */
    public MessageSearchDAO withRead(Boolean value) {
        this.read = value;
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
     * Adds selection order.
     * @param value
     * @return
     */
    public MessageSearchDAO order(Order value) {
        orders.add(value);
        return this;
    }

    /**
     * Executes search.
     * @param result pageable result.
     * @throws SQLException
     */
    public void search(Pageable<Message> result) throws SQLException {
        var page = result.getPage();

        final boolean pinnedFirst = orders.contains(Order.PINNED_FIRST);

        PreparedQuery pq = new PreparedQuery(con);
        pq.addQuery(SQL_SELECT_COUNT_ROWS + "m.*, p.*" + (pinnedFirst ? ", pin_tag.tag_id" : "") + SQL_FROM + TABLE_MESSAGE + "AS m"
                + SQL_LEFT_JOIN + TABLE_PROCESS + "AS p ON m.process_id=p.id");
        if (pinnedFirst) {
            pq.addQuery(SQL_LEFT_JOIN + TABLE_MESSAGE_TAG + "AS pin_tag ON m.id=pin_tag.message_id AND pin_tag.tag_id=?");
            pq.addInt(TagConfig.Tag.TAG_PIN_ID);
        }
        if (CollectionUtils.isNotEmpty(tagIds))
            pq.addQuery(SQL_INNER_JOIN + TABLE_MESSAGE_TAG + " AS mt ON m.id=mt.message_id AND mt.tag_id IN (" + Utils.toString(tagIds) + ")");
        pq.addQuery(SQL_WHERE + "1>0 ");
        if (processIds != null) {
            pq.addQuery(" AND m.process_id IN (");
            pq.addQuery(Utils.toString(processIds));
            pq.addQuery(")");
        }
        if (CollectionUtils.isNotEmpty(typeIds)) {
            pq.addQuery(" AND m.type_id IN (");
            pq.addQuery(Utils.toString(typeIds));
            pq.addQuery(")");
        }
        if (direction != null) {
            pq.addQuery(" AND m.direction=?");
            pq.addInt(direction);
        }
        if (processed != null) {
            if (processed)
                pq.addQuery(" AND process_id!=0");
            else
                pq.addQuery(" AND process_id=0");
        }
        if (read != null) {
            pq.addQuery( " AND to_dt IS");
            if (read)
                pq.addQuery(" NOT");
            pq.addQuery(" NULL");
        }
        if (attach != null) {
            if (attach)
                pq.addQuery(" AND attach_data");
            else
                pq.addQuery(" AND NOT(attach_data)");
        }
        if (dateFrom != null) {
            if (dateFrom.getDateFrom() != null) {
                pq.addQuery(" AND ?<m.from_dt");
                pq.addDate(dateFrom.getDateFrom());
            }
            if (dateFrom.getDateTo() != null) {
                pq.addQuery(" AND m.from_dt<?");
                pq.addDate(TimeUtils.getNextDay(dateFrom.getDateTo()));
            }
        }
        if (Utils.notBlankString(from)) {
            pq.addQuery(" AND m.from LIKE ?");
            pq.addString(from);
        }

        pq.addQuery(SQL_ORDER_BY);
        if (orders.isEmpty()) {
            pq.addQuery("m.from_dt");
        } else {
            boolean first = true;
            for (var order : orders) {
                if (!first)
                    pq.addQuery(", ");

                pq.addQuery(switch (order) {
                    case FROM_TIME -> "m.from_dt";
                    case FROM_TIME_DESC -> "m.from_dt DESC";
                    case PINNED_FIRST -> "pin_tag.tag_id DESC";
                });

                first = false;
            }
        }

        pq.addQuery(getPageLimit(page));

        var rs = pq.executeQuery();
        while (rs.next()) {
            Message message = getMessageFromRs(rs, "m.");
            result.getList().add(message);

            if (rs.getInt("p.id") > 0) {
                message.setProcess(ProcessDAO.getProcessFromRs(rs, "p."));
            }
        }

        setRecordCount(page, pq.getPrepared());
        pq.close();
    }
}
