package org.bgerp.plugin.pln.sla.config;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.pln.sla.Plugin;

public class Config extends org.bgerp.app.cfg.Config {
    private final int paramCloseBeforeId;
    private final int paramUpdateBeforeId;

    protected Config(ConfigMap config) throws InitStopException {
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
