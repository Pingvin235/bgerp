package org.bgerp.plugin.pln.grpl.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.iface.IdTitle;

import javassist.NotFoundException;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.dao.expression.ProcessParamExpressionObject;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class BoardConfig extends Config implements IdTitle<Integer> {
    private final int id;
    private final String title;
    private final Set<Integer> processTypeIds;
    private final Map<Integer, ColumnConfig> columns;
    final int columnMapSize;
    private final String columnExpression;
    private final List<Integer> groupIds;
    private final ShiftConfig shift;
    private final String processDurationExpression;
    private final ConfigMap backgroundColors;

    public BoardConfig(int id, ConfigMap config) {
        super(null);
        this.id = id;
        this.title = config.get("title", "???");
        this.processTypeIds = Utils.toIntegerSet(config.get("process.types"));
        this.columns = loadColumns(config);
        this.columnMapSize = columns.size() + 2;
        this.columnExpression = config.get("column."+ Expression.EXPRESSION_CONFIG_KEY);
        this.groupIds = Utils.toIntegerList(config.get("groups"));
        this.shift = new ShiftConfig(config);
        this.processDurationExpression = config.get("process.duration." + Expression.EXPRESSION_CONFIG_KEY, "30M");
        this.backgroundColors = config.sub("process.background.color.");
    }

    private Map<Integer, ColumnConfig> loadColumns(ConfigMap config) {
        Map<Integer, ColumnConfig> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("column.").entrySet())
            result.put(me.getKey(), new ColumnConfig(me.getKey(), me.getValue()));

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public Set<Integer> getProcessTypeIds() {
        return processTypeIds;
    }

    public Map<Integer, ColumnConfig> getColumns() {
        return columns;
    }

    public int getColumnWidth() {
        return 100 / columnMapSize;
    }

    public ColumnConfig getColumnOrThrow(ConnectionSet conSet, Process process) throws NotFoundException {
        var context = new HashMap<String, Object>();
        new ProcessExpressionObject(process).toContext(context);
        new ProcessParamExpressionObject(conSet.getSlaveConnection(), process.getId()).toContext(context);

        return getColumnOrThrow((Integer) new Expression(context).execute(columnExpression));
    }

    public ColumnConfig getColumnOrThrow(int id) throws NotFoundException {
        var result = columns.get(id);
        if (result == null)
            throw new NotFoundException("Not found column with ID: " + id);
        return result;
    }

    public List<Integer> getGroupIds() {
        return groupIds;
    }

    public ShiftConfig getShift() {
        return shift;
    }

    public Duration getProcessDuration(ConnectionSet conSet, Process process) {
        var context = new HashMap<String, Object>();
        new ProcessExpressionObject(process).toContext(context);
        new ProcessParamExpressionObject(conSet.getSlaveConnection(), process.getId()).toContext(context);

        return Duration.parse("PT" + new Expression(context).executeGetString(processDurationExpression));
    }

    public List<LocalTime> getTimes(List<Slot> usedSlots, Duration processDuration) {
        List<LocalTime> times = new ArrayList<>();

        var time = shift.getFrom();

        for (var slot : usedSlots) {
            // times before the slot
            while (time.plus(processDuration).compareTo(slot.getTime()) <= 0) {
                times.add(time);
                time = time.plus(processDuration);
            }
            time = slot.getTime().plus(slot.getDuration());
        }

        // after the last slot and before end of shift
        while (time.plus(processDuration).compareTo(shift.getTo()) <= 0) {
            times.add(time);
            time = time.plus(processDuration);
        }

        return times;
    }

    public String getProcessBackgroundColor(int statusId) {
        return backgroundColors.getOrDefault(String.valueOf(statusId), "");
    }
}
