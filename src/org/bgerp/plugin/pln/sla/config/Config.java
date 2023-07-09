package org.bgerp.plugin.pln.sla.config;

import org.bgerp.plugin.pln.sla.Plugin;

import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {
    private final int paramCloseBeforeId;
    private final int paramUpdateBeforeId;

    protected Config(ParameterMap config) throws InitStopException {
        super(null);
        config = config.sub(Plugin.ID + ":");
        paramCloseBeforeId = config.getInt("param.close.before");
        paramUpdateBeforeId = config.getInt("param.update.before");
        initWhen(paramCloseBeforeId > 0 || paramUpdateBeforeId > 0);
    }

    public int getParamCloseBeforeId() {
        return paramCloseBeforeId;
    }

    public int getParamUpdateBeforeId() {
        return paramUpdateBeforeId;
    }
}
