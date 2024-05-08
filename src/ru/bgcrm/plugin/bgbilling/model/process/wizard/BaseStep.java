package ru.bgcrm.plugin.bgbilling.model.process.wizard;

import org.bgerp.app.cfg.ConfigMap;

abstract class BaseStep extends ru.bgcrm.model.process.wizard.base.Step {
    protected static final String PATH_JSP = ru.bgcrm.plugin.bgbilling.Plugin.PATH_JSP_USER + "/process/wizard";

    BaseStep(ConfigMap config) {
        super(config);
    }
}
