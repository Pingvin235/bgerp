package org.bgerp.plugin.pln.grpl.model;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.dao.AddressDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.expression.ProcessExpressionObject;
import ru.bgcrm.dao.expression.ProcessParamExpressionObject;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class BoardConfig extends Config implements org.bgerp.model.base.iface.IdTitle<Integer> {
    private static final Log log = Log.getLog();

    private final int id;
    private final String title;
    private final int paramId;
    private final ColumnsConfig columnsConfig;
    private final Map<Integer, IdTitle> columns;
    final int columnMapSize;
    private final List<Integer> groupIds;
    private final ShiftConfig shift;
    private final String processDurationExpression;
    private final ConfigMap backgroundColors;

    public BoardConfig(int id, ConfigMap config) {
        super(null);
        this.id = id;
        this.title = config.get("title", "???");
        this.paramId = config.getInt("on.changed.param");
        this.columnsConfig = new ColumnsConfig(config);
        this.columns = loadColumns(config);
        this.columnMapSize = columns.size() + 2;
        this.groupIds = Utils.toIntegerList(config.get("groups"));
        this.shift = new ShiftConfig(config);
        this.processDurationExpression = config.get("process.duration." + Expression.EXPRESSION_CONFIG_KEY, "30M");
        this.backgroundColors = config.sub("process.background.color.");
    }

    private Map<Integer, IdTitle> loadColumns(ConfigMap config) {
        Map<Integer, IdTitle> result = new LinkedHashMap<>();

        if (columnsConfig.type == ColumnsConfig.Type.CITY) {
            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                var cities = new AddressDAO(con).getAddressCities(columnsConfig.cityIds);
                // using stream-collect breaks the order
                for (IdTitle city : cities)
                    result.put(city.getId(), city);
            } catch (SQLException e) {
                log.error(e);
            }
        }

        // title correction
        result.values().stream().forEach(city -> city.setTitle(config.get("column." + city.getId() + ".title", city.getTitle())));

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

    public int getParamId() {
        return paramId;
    }

    public Map<Integer, IdTitle> getColumns() {
        return columns;
    }

    public int getColumnWidth() {
        return 100 / columnMapSize;
    }

    public IdTitle getColumnOrThrow(ConnectionSet conSet, Process process) throws NotFoundException, SQLException {
        int columnId = 0;

        if (columnsConfig.type == ColumnsConfig.Type.CITY) {
            Parameter param = columnsConfig.param;
            if (param == null || !Parameter.TYPE_ADDRESS.equals(param.getType()))
                log.error("Missing parameter or type not 'address'");
            else {
                ParamValueDAO dao = new ParamValueDAO(conSet.getConnection());
                ParameterAddressValue value = Utils.getFirst(dao.getParamAddress(process.getId(), param.getId(), true).values());
                if (value != null)
                    columnId = value.getHouse().getAddressStreet().getCityId();
            }
        }

        return getColumnOrThrow(columnId);
    }

    public IdTitle getColumnOrThrow(int id) throws NotFoundException {
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
