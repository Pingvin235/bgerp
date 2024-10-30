package org.bgerp.plugin.pln.grpl.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.cache.UserCache;
import org.bgerp.util.TimeConvert;

public class Row {
    private final BoardConfig board;
    private final Date date;
    private final Map<Integer, Cell> cells;

    public Row(BoardConfig board, Date date) {
        this.board = board;
        this.date = date;
        this.cells = new HashMap<>(board.columnMapSize);
    }

    public Row(BoardConfig board, ResultSet rs) throws SQLException {
        this(board, rs.getDate("date"));
    }

    public BoardConfig getBoard() {
        return board;
    }

    public Date getDate() {
        return date;
    }

    public boolean isWorkingDay() {
        var dw = TimeConvert.toLocalDate(date).getDayOfWeek();
        return dw != DayOfWeek.SATURDAY && dw != DayOfWeek.SUNDAY;
    }

    public Cell setCell(int columnId, int groupId) {
        var result = new Cell(this, columnId, UserCache.getUserGroup(groupId));
        cells.put(columnId, result);
        return result;
    }

    public Cell getCell(int columnId) {
        return cells.get(columnId);
    }

    Set<Integer> excludeGroupIds() {
        return cells.values().stream()
            .filter(cell -> cell.getGroup() != null)
            .map(cell -> cell.getGroup().getId())
            .collect(Collectors.toSet());
    }
}
