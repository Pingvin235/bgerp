package org.bgerp.plugin.msg.email;

import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.util.ParameterMap;

public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "email";

    public static final String PATH_JSP_USER = BaseAction.PATH_JSP_USER_PLUGIN + "/" + ID;

    public Plugin() {
        super(ID);
    }

    @Override
    public boolean isEnabled(ParameterMap config, String defaultValue) {
        // TODO: Always enabled for now.
        return true;
    }

}