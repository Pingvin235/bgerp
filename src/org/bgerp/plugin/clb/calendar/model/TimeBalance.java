package org.bgerp.plugin.clb.calendar.model;

import java.util.HashMap;
import java.util.Map;

import org.bgerp.plugin.clb.calendar.Config;
import org.bgerp.util.Log;

import ru.bgcrm.util.Setup;

public class TimeBalance {
    private final int userId;
    private final int year;
    /** Time in minutes. */
    private int in;
    /** Key - event type ID. */
    private final Map<Integer, Integer> eventAmounts;
    private int out;

    public TimeBalance(int userId, int year) {
        this.userId = userId;
        this.year = year;
        var config = Setup.getSetup().getConfig(Config.class);
        this.eventAmounts = new HashMap<>(config.getBalanceEventTypeList().size());
    }

    public int getUserId() {
        return userId;
    }

    public int getYear() {
        return year;
    }

    public int getIn() {
        return in;
    }

    public void setIn(int value) {
        this.in = value;
    }

    public int getOut() {
        return out;
    }

    public void setOut(int out) {
        this.out = out;
    }

    public void addAccount(TimeAccount value) {
        var config = Setup.getSetup().getConfig(Config.class);

        int typeId = value.getTypeId();
        if (typeId == TimeAccount.TYPE_IN) {
            if (in != 0)
                throw new IllegalStateException(Log.format("IN account is already set to {}; userId: {}, year: {}", in, userId, year));
            in = value.getAmount();
            return;
        }

        var type = config.getEventType(typeId);
        if (type == null)
            throw new IllegalArgumentException(Log.format("Not found event type {}; userId: {}, year: {}", typeId, userId, year));

        eventAmounts.put(typeId, eventAmounts.computeIfAbsent(typeId, unused -> 0) + value.getAmount());

        updateOut();
    }

    private void updateOut() {
        out = in + eventAmounts.values().stream().reduce(0, Integer::sum);
    }
}
