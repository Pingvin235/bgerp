package org.bgerp.plugin.pln.blow.dao;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bgerp.dao.process.ProcessQueueDAO;
import org.bgerp.plugin.pln.blow.model.BoardConfig;

import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;

public class BoardDAO extends ProcessQueueDAO {
    public BoardDAO(Connection con) {
        super(con);
    }

    public BoardDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * Возвращает набор все процессов для визуализаци.
     * Сортировка обратная по приоритету.
     * Первый параметр в очереди - процесс, остальные - настроенные колонки.
     * @param board
     * @return
     */
    public List<Pair<Process, Map<String, Object>>> getProcessList(BoardConfig board) throws Exception {
        List<Pair<Process, Map<String, Object>>> result = new ArrayList<>();

        QueueSelectParams qsp = prepareQueueSelect(board.getQueue());
        addFilters(board.getQueue(), DynActionForm.SYSTEM_FORM, qsp);
        qsp.wherePart.append(" AND process.close_dt IS NULL");

        final int columns = qsp.queue.getColumnMap().size();
        // column names col1..colX
        List<String> columnNames = qsp.queue.getColumnMap().keySet().stream()
                .map(id -> "col" + id).collect(Collectors.toList());

        StringBuilder query = new StringBuilder(2000);
        query.append("SELECT DISTINCT SQL_CALC_FOUND_ROWS ");
        query.append(qsp.selectPart);
        query.append(" FROM " + TABLE_PROCESS + " AS process");
        query.append(qsp.joinPart);
        query.append(qsp.wherePart);

        PreparedStatement ps = con.prepareStatement(query.toString());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Map<String, Object> params = new HashMap<>(columns);
            for (int i = 1; i <= columns; i++)
               params.put(columnNames.get(i - 1), rs.getObject(i));
            result.add(new Pair<>(getProcessFromRs(rs), params));
        }
        ps.close();

        return result;
    }

}
