package org.bgerp.plugin.pln.callboard.model.config;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.model.param.Parameter;
import org.bgerp.plugin.pln.callboard.model.config.CallboardConfig.Callboard;

public class ProcessTimeSetConfig extends Config {
    private static final String CONFIG_PREFIX = "callboard.timeset.";

    private final Callboard callboard;
    private final Parameter param;
    private final int daysShow;
    private final int changeStatusToId;

    public ProcessTimeSetConfig(ConfigMap config) throws BGException {
        super(null);

        callboard = Setup.getSetup().getConfig(CallboardConfig.class).get(config.getInt(CONFIG_PREFIX + "graphId", 0));
        param = ParameterCache.getParameter(config.getInt(CONFIG_PREFIX + "paramId", 0));
        daysShow = config.getInt(CONFIG_PREFIX + "daysShow", 3);
        changeStatusToId = config.getInt(CONFIG_PREFIX + "changeStatusToId", -1);
    }

    public Callboard getCallboard() {
        return callboard;
    }

    public Parameter getParam() {
        return param;
    }

    public int getDaysShow() {
        return daysShow;
    }

    public int getChangeStatusToId() {
        return changeStatusToId;
    }
}
