package org.bgerp.plugin.pln.grpl.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.bgerp.plugin.pln.grpl.model.BoardConfig;
import org.bgerp.plugin.pln.grpl.model.Cell;
import org.bgerp.plugin.pln.grpl.model.Row;
import org.bgerp.plugin.pln.grpl.model.Slot;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.util.TimeUtils;

public class GrplDAO extends CommonDAO {
    public GrplDAO(Connection con) {
        super(con);
    }

    public List<Row> getRows(BoardConfig board, Date dateFrom, Date dateTo) throws SQLException {
        SortedMap<Date, Row> result = new TreeMap<>();

        String query = SQL_SELECT + "bg.date, bg.column_id, bg.group_id, bp.process_id, p.*, bp.time, bp.duration"
            + SQL_FROM + Tables.TABLE_BOARD_GROUP + "AS bg"
            + SQL_LEFT_JOIN + Tables.TABLE_BOARD_PROCESS + "AS bp ON bg.board_id=bp.board_id AND bg.date=bp.date AND bg.column_id=bp.column_id"
            + SQL_LEFT_JOIN + ru.bgcrm.dao.process.Tables.TABLE_PROCESS + "AS p ON bp.process_id=p.id"
            + SQL_WHERE + "bg.board_id=?" + SQL_AND + "?<=bg.date" + SQL_AND + "bg.date<=?"
            + SQL_ORDER_BY + "date, time IS NULL, time";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(board.getId());
            pq.addDate(dateFrom);
            pq.addDate(dateTo);

            Row row = null;

            var rs = pq.executeQuery();
            while (rs.next()) {
                Date date = rs.getDate("date");
                if (row == null || !row.getDate().equals(date)) {
                    row = new Row(board, date);
                    result.put(row.getDate(), row);
                }

                int columnId = rs.getInt("column_id");
                int groupId = rs.getInt("group_id");

                if (columnId <= 0 || groupId <= 0)
                    continue;

                var cell = row.getCell(columnId);
                if (cell == null)
                    cell = row.setCell(columnId, groupId);

                int processId = rs.getInt("process_id");
                if (processId <= 0)
                    continue;

                cell.getSlots().add(new Slot(cell, ProcessDAO.getProcessFromRs(rs, "p."), rs));
            }
        }

        while (!dateFrom.after(dateTo)) {
            result.computeIfAbsent(dateFrom, date -> new Row(board, date));
            dateFrom = TimeUtils.getNextDay(dateFrom);
        }

        // last row with process queues
        Row row = new Row(board, (Date) null);

        query = SQL_SELECT + "bp.column_id, bp.duration, bp.time, p.*" + SQL_FROM + Tables.TABLE_BOARD_PROCESS + "AS bp"
            + SQL_LEFT_JOIN + ru.bgcrm.dao.process.Tables.TABLE_PROCESS + "AS p ON bp.process_id=p.id"
            + SQL_WHERE + "bp.board_id=? AND bp.date IS NULL";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(board.getId());

            var rs = pq.executeQuery();
            while (rs.next()) {
                int columnId = rs.getInt("column_id");

                var cell = row.getCell(columnId);
                if (cell == null)
                    cell = row.setCell(columnId, 0);

                cell.getSlots().add(new Slot(cell, ProcessDAO.getProcessFromRs(rs, "p."), rs));
            }
        }

        return row.hasCells() ?
            Stream.concat(result.values().stream(), Stream.of(row)).toList() :
            result.values().stream().toList();
    }

    public Row getRow(BoardConfig board, Date date) throws SQLException {
        Row result = new Row(board, date);

        String query = SQL_SELECT_ALL_FROM + Tables.TABLE_BOARD_GROUP + SQL_WHERE + "board_id=?" + SQL_AND + "date=?";
        try (var pq = new PreparedQuery(con, query)) {
            pq.addInt(board.getId());
            pq.addDate(date);

            var rs = pq.executeQuery();
            while (rs.next())
                result.setCell(rs.getInt("column_id"), rs.getInt("group_id"));
        }

        return result;
    }

    public void updateGroup(int boardId, Date date, int columnId, int groupId) throws SQLException {
        if (groupId > 0)
            updateOrInsert(
                SQL_UPDATE + Tables.TABLE_BOARD_GROUP + "SET group_id=?" + SQL_WHERE + "board_id=? AND date=? AND column_id=?",
                SQL_INSERT_INTO + Tables.TABLE_BOARD_GROUP + "(group_id, board_id, date, column_id)" + SQL_VALUES + "(?,?,?,?)",
                groupId, boardId, date, columnId);
        else
            try (var pq = new PreparedQuery(con,
                    SQL_DELETE_FROM + Tables.TABLE_BOARD_GROUP + SQL_WHERE + "board_id=? AND date=? AND column_id=?")) {
                pq.addInt(boardId);
                pq.addDate(date);
                pq.addInt(columnId);
                pq.executeUpdate();
            }
    }

    public Slot getSlot(BoardConfig board, Process process) throws SQLException {
        Slot result = null;

        try (var pq = new PreparedQuery(con, SQL_SELECT + "bp.column_id, bp.duration, bp.date, bp.time, bg.group_id"
            + SQL_FROM + Tables.TABLE_BOARD_PROCESS + "AS bp"
            + SQL_LEFT_JOIN + Tables.TABLE_BOARD_GROUP + "AS bg ON bp.board_id=bg.board_id AND bp.column_id=bg.column_id AND bp.date=bg.date"
            + SQL_WHERE + "bp.board_id=? AND bp.process_id=?")) {
            pq.addInt(board.getId());
            pq.addInt(process.getId());

            var rs = pq.executeQuery();
            if (rs.next()) {
                Row row = new Row(board, rs);
                Cell cell = row.setCell(rs.getInt("column_id"), rs.getInt("group_id"));
                result = new Slot(cell, process, rs);
            }
        }

        return result;
    }

    /* public List<Slot> getSlots(BoardConfig board, Date date, int columnId, int excludeProcessId) throws SQLException {
        Row row = new Row(board, date);
        Cell cell = row.setCell(columnId, 0);

        try (var pq = new PreparedQuery(con, SQL_SELECT + "bp.date, bp.duration, bp.time" + SQL_FROM + Tables.TABLE_BOARD_PROCESS + "AS bp"
            + SQL_WHERE + "bp.board_id=? AND bp.date=? AND bp.column_id=? AND bp.process_id!=?")) {
            pq.addInt(board.getId());
            pq.addDate(date);
            pq.addInt(columnId);
            pq.addInt(excludeProcessId);

            var rs = pq.executeQuery();
            while (rs.next())
                cell.getSlots().add(new Slot(cell, null, rs));
        }

        return cell.getSlots();
    }

    public List<Row> getRows(BoardConfig board, Date dateFrom, int columnId) throws SQLException {
        var result = new TreeMap<Date, Row>();

        try (var pq = new PreparedQuery(con, SQL_SELECT + "bg.date, bg.column_id, bg.group_id, p.*, bp.duration, bp.time"
            + SQL_FROM + Tables.TABLE_BOARD_GROUP + "AS bg"
            + SQL_LEFT_JOIN + Tables.TABLE_BOARD_PROCESS + "AS bp ON bg.board_id=bp.board_id AND bg.date=bp.date AND bg.column_id=bp.column_id"
            + SQL_LEFT_JOIN + ru.bgcrm.dao.process.Tables.TABLE_PROCESS + "AS p ON bp.process_id=p.id"
            + SQL_WHERE + "bg.board_id=? AND ?<=bg.date AND bg.column_id=?"
            + SQL_ORDER_BY + "date")) {
            pq.addInt(board.getId());
            pq.addDate(dateFrom);
            pq.addInt(columnId);

            Row row = null;

            var rs = pq.executeQuery();
            while (rs.next()) {
                Date date = rs.getDate("date");
                if (row == null || !row.getDate().equals(date)) {
                    row = new Row(board, date);
                    result.put(row.getDate(), row);
                }

                var cell = row.getCell(columnId);
                if (cell == null)
                    cell = row.setCell(columnId, rs.getInt("group_id"));

                Process process = ProcessDAO.getProcessFromRs(rs, "p.");
                if (process.getId() != 0)
                    cell.getSlots().add(new Slot(cell, process, rs));
            }
        }

        return result.values().stream().toList();
    }*/

    public void updateSlot(BoardConfig board, Process process, int columnId, Duration duration) throws SQLException {
        updateOrInsert(
            SQL_UPDATE + Tables.TABLE_BOARD_PROCESS + SQL_SET + "column_id=?, duration=?" + SQL_WHERE + "board_id=? AND process_id=?",
            SQL_INSERT_INTO + Tables.TABLE_BOARD_PROCESS + "(column_id, duration, board_id, process_id)" + SQL_VALUES + "(?,?,?,?)",
            columnId, duration.toMinutes(), board.getId(), process.getId());

        // update process groups
        var slot = getSlot(board, process);

        var groups = process.getGroups();
        groups.removeIf(pg -> board.getGroupIds().contains(pg.getGroupId()));
        Group group = slot.getCell().getGroup();
        if (group != null)
            groups.add(new ProcessGroup(group.getId()));

        new ProcessDAO(con).updateProcessGroups(groups, process.getId());
    }

    /* public void deleteSlot(int boardId, int processId) throws SQLException {
        try (var pq = new PreparedQuery(con, SQL_DELETE_FROM + Tables.TABLE_BOARD_PROCESS + SQL_WHERE + "board_id=? AND process_id=?")) {
            pq.addInt(boardId);
            pq.addInt(processId);
            pq.executeUpdate();
        }
    } */

    public void updateSlotTime(int boardId, int processId, Date date, LocalTime time) throws SQLException {
        try (var pq = new PreparedQuery(con,
                SQL_UPDATE + Tables.TABLE_BOARD_PROCESS + SQL_SET + "date=?, time=?" + SQL_WHERE + "board_id=? AND process_id=?")) {
            pq.addObjects(date, time, boardId, processId);
            pq.executeUpdate();
        }
    }
}
