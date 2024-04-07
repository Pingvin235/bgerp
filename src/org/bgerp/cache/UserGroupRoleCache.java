package org.bgerp.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class UserGroupRoleCache extends Cache<UserGroupRoleCache> {
    private static CacheHolder<UserGroupRoleCache> holder = new CacheHolder<UserGroupRoleCache>(new UserGroupRoleCache());

    static {
        EventProcessor.subscribe(new EventListener<SetupChangedEvent>() {
            @Override
            public void notify(SetupChangedEvent e, ConnectionSet connectionSet) {
                holder.flush(connectionSet.getConnection());
            }
        }, SetupChangedEvent.class);
    }

    public static List<IdTitle> getUserGroupRoleList() {
        return holder.getInstance().userGroupRoleList;
    }

    public static Map<Integer, IdTitle> getUserGroupRoleMap() {
        return holder.getInstance().userGroupRoleMap;
    }

    // конец статической части

    private Map<Integer, IdTitle> userGroupRoleMap;
    private List<IdTitle> userGroupRoleList;

    @Override
    protected UserGroupRoleCache newInstance() {
        UserGroupRoleCache result = new UserGroupRoleCache();

        result.userGroupRoleList = Utils.parseIdTitleList(Setup.getSetup().get("processGroupRoles", "0:Выполнение"));
        result.userGroupRoleMap = new HashMap<Integer, IdTitle>(result.userGroupRoleList.size());
        for (IdTitle role : result.userGroupRoleList) {
            result.userGroupRoleMap.put(role.getId(), role);
        }

        return result;
    }
}