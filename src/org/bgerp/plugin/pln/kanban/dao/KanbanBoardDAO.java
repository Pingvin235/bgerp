package org.bgerp.plugin.pln.kanban.dao;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.bgerp.dao.process.ProcessQueueDAO;

import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class KanbanBoardDAO extends ProcessQueueDAO {
    public KanbanBoardDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * @param queue
     * @param typeId single process type to narrow down to; must be one of {@code queue.getProcessTypeIds()}
     * @return processes ordered per the queue's selected sort combo(s), same as the request would order a plain queue table
     */
    public List<Process> getProcesses(Queue queue, int typeId) throws Exception {
        List<Process> result = new ArrayList<>();

        QueueSelectParams qsp = prepareQueueSelect(queue);
        addFilters(queue, form, qsp);
        qsp.wherePart.append(" AND process.type_id=").append(typeId);

        StringBuilder query = new StringBuilder(2000)
            .append(SQL_SELECT)
            .append(qsp.selectPart)
            .append(SQL_FROM + TABLE_PROCESS + "AS process")
            .append(qsp.joinPart)
            .append(qsp.wherePart)
            .append(SQL_GROUP_BY + "process.id");

        String orders = queue.getSortSet().getOrders(form);
        if (Utils.notBlankString(orders)) {
            query.append(SQL_ORDER_BY).append(orders);
        }

        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(getProcessFromRs(rs));
        }

        return result;
    }
}
