package org.bgerp.plugin.kernel;

import java.util.Set;

import org.bgerp.action.admin.RunAction;

import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.Tables;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.Table;
import ru.bgcrm.plugin.Table.Type;
import ru.bgcrm.struts.action.admin.AppAction;
import ru.bgcrm.util.ParameterMap;

/**
 * Special plugin for application's kernel.
 * @author Shamil Vakhitov
 */
public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "kernel";

    public Plugin() {
        super(ID);
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public boolean isEnabled(ParameterMap config, String defaultValue) {
        return true;
    }

    public Set<String> getActionPackages() {
        return Set.of(
            "org.bgerp.action",
            "ru.bgcrm.struts.action"
        );
    }

    @Override
    public Set<Table> getTables() {
        // used the order same with db.sql
        return Set.of(
            new Table(Tables.TABLE_ADDRESS_AREA, Type.DIRECTORY), 
            new Table(Tables.TABLE_ADDRESS_CITY, Type.DIRECTORY),
            new Table(Tables.TABLE_ADDRESS_CONFIG, Type.DIRECTORY), 
            new Table(Tables.TABLE_ADDRESS_COUNTRY, Type.DIRECTORY),
            new Table(Tables.TABLE_ADDRESS_HOUSE, Type.DIRECTORY),
            new Table(Tables.TABLE_ADDRESS_QUARTER, Type.DIRECTORY),
            new Table(Tables.TABLE_ADDRESS_STREET, Type.DIRECTORY),
            new Table(IfaceStateDAO.TABLE_NAME, Type.TRASH)
        );
    }

    @Override
    public Set<String> getObjectTypes() {
        return Set.of(
            User.OBJECT_TYPE,
            Process.OBJECT_TYPE,
            Customer.OBJECT_TYPE);
    }

    @Override
    public Set<String> getUnusedPaths() {
        return Set.of(
            "lib/app/bgcrm.jar", 
            "plugin"
        );
    }
}
