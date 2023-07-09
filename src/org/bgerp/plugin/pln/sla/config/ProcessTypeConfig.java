package org.bgerp.plugin.pln.sla.config;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.plugin.pln.sla.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessTypeConfig extends ru.bgcrm.util.Config {
    private static final Log log = Log.getLog();

    private final Duration closeBefore;
    private final Duration updateBefore;
    private final SortedMap<Long, String> leftMinutesColors;

    protected ProcessTypeConfig(ParameterMap config) throws InitStopException {
        super(null);
        var subConfig = config.sub(Plugin.ID + ":");
        closeBefore = loadDurationInMin(subConfig, "close.before.minutes");
        updateBefore = loadDurationInMin(subConfig, "update.before.minutes");
        leftMinutesColors = loadLeftMinutesColors(subConfig);
        log.debug("closeBefore: {}, updateBefore: {}, config: {}", closeBefore, updateBefore, config);
        initWhen(closeBefore != null || updateBefore != null);
    }

    private Duration loadDurationInMin(ParameterMap config, String key) {
        int value = config.getInt(key);
        return value > 0 ? Duration.ofMinutes(value) : null;
    }

    private SortedMap<Long, String> loadLeftMinutesColors(ParameterMap config) {
        var result = new TreeMap<Long, String>();

        // sla:color.yellow.when.left.minutes=20
        for (var me : config.sub("color.").entrySet())
            result.put(Utils.parseLong(me.getValue()), StringUtils.substringBefore(me.getKey(), "."));

        return Collections.unmodifiableSortedMap(result);
    }

    public SortedMap<Long, String> getLeftMinutesColors() {
        return leftMinutesColors;
    }

    public void processCreated(ConnectionSet conSet, Config config, int processId) {
        try {
            var dao = new ParamValueDAO(conSet.getConnection());
            if (closeBefore != null && config.getParamCloseBeforeId() > 0)
                dao.updateParamDateTime(processId, config.getParamCloseBeforeId(), Date.from(Instant.now().plus(closeBefore)));
            if (updateBefore != null && config.getParamUpdateBeforeId() > 0)
                dao.updateParamDateTime(processId, config.getParamUpdateBeforeId(), Date.from(Instant.now().plus(updateBefore)));
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public void processUpdated(ConnectionSet conSet, Config config, int processId) {
        try {
            var dao = new ParamValueDAO(conSet.getConnection());
            if (updateBefore != null && config.getParamUpdateBeforeId() > 0)
                dao.updateParamDateTime(processId, config.getParamUpdateBeforeId(), Date.from(Instant.now().plus(updateBefore)));
        } catch (SQLException e) {
            log.error(e);
        }
    }
}
