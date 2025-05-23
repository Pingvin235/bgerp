package org.bgerp.plugin.kernel;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.event.listener.ProcessTextListener;

import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.Locker;
import ru.bgcrm.event.listener.CustomerTitleListener;
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

/**
 * Special plugin for application's kernel.
 *
 * @author Shamil Vakhitov
 */
public class Plugin extends ru.bgcrm.plugin.Plugin {
    public static final String ID = "kernel";
    public static final Plugin INSTANCE = new Plugin();

    public static final String ENDPOINT_MESSAGE_EDITOR = BaseAction.PATH_JSP_USER + "/message/process/edit/note/editor.jsp";

    private Plugin() {
        super(ID);
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public boolean isEnabled(ConfigMap config, String defaultValue) {
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
            new Table(org.bgerp.dao.param.Tables.TABLE_ADDRESS_AREA, Type.DIRECTORY),
            new Table(org.bgerp.dao.param.Tables.TABLE_ADDRESS_CITY, Type.DIRECTORY),
            new Table(org.bgerp.dao.param.Tables.TABLE_ADDRESS_COUNTRY, Type.DIRECTORY),
            new Table(org.bgerp.dao.param.Tables.TABLE_ADDRESS_HOUSE, Type.DIRECTORY),
            new Table(org.bgerp.dao.param.Tables.TABLE_ADDRESS_QUARTER, Type.DIRECTORY),
            new Table(org.bgerp.dao.param.Tables.TABLE_ADDRESS_STREET, Type.DIRECTORY),
            new Table(IfaceStateDAO.TABLE_NAME, Type.TRASH),
            // update logs in ExecuteSQL
            new Table("sql_patches_history", Type.DEPRECATED),
            new Table("analytic_house_capacity", Type.DEPRECATED),
            new Table("address_config", Type.DEPRECATED)
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
    public org.bgerp.dao.Cleaner getCleaner() {
        return Cleaner.INSTANCE;
    }

    @Override
    protected Map<String, List<String>> endpoints() {
        return Map.of(
            ENDPOINT_MESSAGE_EDITOR, List.of(ENDPOINT_MESSAGE_EDITOR)
        );
    }

    @Override
    public void init(Connection con) throws Exception {
        super.init(con);

        // event listeners
        new CustomerTitleListener();
        new ProcessTextListener();
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
