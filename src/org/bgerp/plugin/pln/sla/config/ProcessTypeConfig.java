package org.bgerp.plugin.pln.sla.config;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.plugin.pln.sla.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessTypeConfig extends org.bgerp.app.cfg.Config {
    private static final Log log = Log.getLog();

    private final Duration closeBefore;
    private final Duration updateBefore;
    private final SortedMap<Long, String> leftMinutesColors;

    protected ProcessTypeConfig(ConfigMap config) throws InitStopException {
        super(null);
        var subConfig = config.sub(Plugin.ID + ":");
        closeBefore = loadDurationInMin(subConfig, "close.before.minutes");
        updateBefore = loadDurationInMin(subConfig, "update.before.minutes");
        leftMinutesColors = loadLeftMinutesColors(subConfig);
        log.debug("closeBefore: {}, updateBefore: {}, config: {}", closeBefore, updateBefore, config);
        initWhen(closeBefore != null || updateBefore != null);
    }

    private Duration loadDurationInMin(ConfigMap config, String key) {
        int value = config.getInt(key);
        return value > 0 ? Duration.ofMinutes(value) : null;
    }

    private SortedMap<Long, String> loadLeftMinutesColors(ConfigMap config) {
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
