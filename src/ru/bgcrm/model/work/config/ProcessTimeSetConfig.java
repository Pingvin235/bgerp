package ru.bgcrm.model.work.config;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.work.config.CallboardConfig.Callboard;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;

public class ProcessTimeSetConfig extends Config {
    private static final String CONFIG_PREFIX = "callboard.timeset.";

    private final Callboard callboard;
    private final Parameter param;
    private final int daysShow;
    private final int changeStatusToId;

    public ProcessTimeSetConfig(ParameterMap config) throws BGException {
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
