package ru.bgerp.plugin.blow.dao;

import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgerp.plugin.blow.model.BoardConfig;

public class BoardDAO extends ProcessDAO {

    public BoardDAO(Connection con, User user) {
        super(con, user);
    }
    
    public BoardDAO(Connection con) {
        super(con);
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
        qsp.wherePart.append(" AND process.close_dt IS NULL");
        
        final int columns = qsp.queue.getColumnMap().size();
        // список имён колонок col1..colX
        List<String> columnNames = qsp.queue.getColumnMap().keySet().stream()
                .map(id -> "col" + id).collect(Collectors.toList());
        
        StringBuilder query = new StringBuilder(2000);
        query.append("SELECT DISTINCT SQL_CALC_FOUND_ROWS ");
        query.append(qsp.selectPart);
        query.append(" FROM " + TABLE_PROCESS + " AS process");
        query.append(qsp.joinPart);
        query.append(qsp.wherePart);
        query.append(SQL_ORDER_BY);
        query.append("process.priority DESC");
        
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
