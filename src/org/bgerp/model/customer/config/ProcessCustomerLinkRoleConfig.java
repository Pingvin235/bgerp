package org.bgerp.model.customer.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.plugin.kernel.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

@Bean(oldClasses = "ru.bgcrm.model.customer.config.ProcessLinkModesConfig")
public class ProcessCustomerLinkRoleConfig extends Config {
    private static final Log log = Log.getLog();

    private final Map<String, String> map = new HashMap<>();
    private final List<IdStringTitle> list = new ArrayList<>();

    public ProcessCustomerLinkRoleConfig(ConfigMap config) {
        super(null);
        for (String token : Utils.toList(config.getSok("process.customer.link.roles", "processCustomerLinkRoles",
                "customer:" + Plugin.INSTANCE.getLocalizer().l("Customer")))) {
            String[] pair = token.trim().split(":");
            if (pair.length == 2) {
                IdStringTitle item = new IdStringTitle(pair[0], pair[1]);
                map.put(item.getId(), item.getTitle());
                list.add(item);
            }
        }
    }

    public Map<String, String> getMap() {
        return map;
    }

    public List<IdStringTitle> getList() {
        return list;
    }

    @Deprecated
    public Map<String, String> getModeMap() {
        log.warndMethod("getModeMap", "getMap");
        return getMap();
    }

    @Deprecated
    public List<IdStringTitle> getModeList() {
        log.warndMethod("getModeList", "getList");
        return getList();
    }
}
