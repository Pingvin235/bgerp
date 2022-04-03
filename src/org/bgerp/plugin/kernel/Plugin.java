package org.bgerp.plugin.kernel;

import java.sql.Connection;
import java.util.Set;

import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.Locker;
import ru.bgcrm.dao.Tables;
import ru.bgcrm.event.listener.CustomerSystemListener;
import ru.bgcrm.event.listener.DefaultProcessChangeListener;
import ru.bgcrm.event.listener.Files;
import ru.bgcrm.event.listener.LoginEventListener;
import ru.bgcrm.event.listener.NewsEventListener;
import ru.bgcrm.event.listener.ParamValidatorSystemListener;
import ru.bgcrm.event.listener.ProcessClosingListener;
import ru.bgcrm.event.listener.ProcessFilterCounterListener;
import ru.bgcrm.event.listener.TemporaryObjectOpenListener;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.Table;
import ru.bgcrm.plugin.Table.Type;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.distr.call.ExecuteSQL;

/**
 * Special plugin for application's kernel.
 *
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
            new Table(IfaceStateDAO.TABLE_NAME, Type.TRASH),
            new Table(ExecuteSQL.TABLE_SQL_PATCHES_HISTORY, Type.DEPRECATED)
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

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        // event listeners
        new CustomerSystemListener();
        new ParamValidatorSystemListener();
        new ProcessClosingListener();
        new DefaultProcessChangeListener();
        new NewsEventListener();
        new Locker();
        new LoginEventListener();
        new ProcessFilterCounterListener();
        new TemporaryObjectOpenListener();
        new Files();
    }
}
