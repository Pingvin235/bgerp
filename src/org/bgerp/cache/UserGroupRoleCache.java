package org.bgerp.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class UserGroupRoleCache extends Cache<UserGroupRoleCache> {
    private static final CacheHolder<UserGroupRoleCache> HOLDER = new CacheHolder<>(new UserGroupRoleCache());

    static {
        EventProcessor.subscribe(new EventListener<>() {
            @Override
            public void notify(SetupChangedEvent e, ConnectionSet connectionSet) {
                HOLDER.flush(connectionSet.getConnection());
            }
        }, SetupChangedEvent.class);
    }

    public static List<IdTitle> getUserGroupRoleList() {
        return HOLDER.getInstance().userGroupRoleList;
    }

    public static Map<Integer, IdTitle> getUserGroupRoleMap() {
        return HOLDER.getInstance().userGroupRoleMap;
    }

    // end of static part

    private Map<Integer, IdTitle> userGroupRoleMap;
    private List<IdTitle> userGroupRoleList;

    @Override
    protected UserGroupRoleCache newInstance() {
        UserGroupRoleCache result = new UserGroupRoleCache();

        result.userGroupRoleList = Utils.parseIdTitleList(Setup.getSetup().get("processGroupRoles", "0:Выполнение"));
        result.userGroupRoleMap = new HashMap<>(result.userGroupRoleList.size());
        for (IdTitle role : result.userGroupRoleList) {
            result.userGroupRoleMap.put(role.getId(), role);
        }

        return result;
    }
}