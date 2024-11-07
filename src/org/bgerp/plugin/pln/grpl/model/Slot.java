package org.bgerp.plugin.pln.grpl.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;

import ru.bgcrm.model.process.Process;

public class Slot {
    private final Cell cell;
    private final Process process;
    private final LocalTime time;
    private final Duration duration;

    Slot(Cell cell, Process process, LocalTime time, Duration duration) {
        this.cell = cell;
        this.process = process;
        this.time = time;
        this.duration = duration;
    }

    public Slot(Cell cell, Process process, ResultSet rs) throws SQLException {
        this(cell, process, rs == null ? null : rs.getObject("time", LocalTime.class), rs == null ? null : Duration.ofMinutes(rs.getInt("duration")));
    }

    public Cell getCell() {
        return cell;
    }

    public Process getProcess() {
        return process;
    }

    public LocalTime getTime() {
        return time;
    }

    public Duration getDuration() {
        return duration;
    }
}
