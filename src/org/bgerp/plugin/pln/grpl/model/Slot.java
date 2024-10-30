package org.bgerp.plugin.pln.grpl.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import ru.bgcrm.model.process.Process;

public class Slot {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final Cell cell;
    private final Process process;
    private final Duration duration;
    private final LocalTime time;

    public Slot(Cell cell, Process process, ResultSet rs) throws SQLException {
        this.cell = cell;
        this.process = process;
        this.duration = Duration.ofMinutes(rs.getInt("duration"));
        this.time = rs.getObject("time", LocalTime.class);
    }

    public Cell getCell() {
        return cell;
    }

    public Process getProcess() {
        return process;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getFormattedTime() {
        return TIME_FORMAT.format(time);
    }
}
