package org.bgerp.plugin.pln.workload.model;

import java.util.Collections;
import java.util.Set;

import ru.bgcrm.util.Utils;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.plugin.pln.workload.Plugin;

public class GroupLoadConfig extends Config {
    private final boolean enabled;
    private final Set<Integer> userGroupIds;
    private final Set<Integer> processTypeIds;

    private final int dateFromParamId;
    private final int dateToParamId;
    private final int addressParamId;

    protected GroupLoadConfig(ConfigMap config, boolean validate) {
        super(null, validate);
        String prefix = Plugin.ID + ":groupLoad.";
        this.userGroupIds = Collections.unmodifiableSet(Utils.toIntegerSet(config.get(prefix + "userGroupIds")));
        this.processTypeIds = Collections.unmodifiableSet(Utils.toIntegerSet(config.get(prefix + "processTypeIds")));
        this.enabled = !userGroupIds.isEmpty() || !processTypeIds.isEmpty();

        this.dateFromParamId = config.getInt(prefix + "dateFromParamId");
        this.dateToParamId = config.getInt(prefix + "dateToParamId");

        this.addressParamId = config.getInt(prefix + "addressParamId");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<Integer> getUserGroupIds() {
        return userGroupIds;
    }

    public Set<Integer> getProcessTypeIds() {
        return processTypeIds;
    }

    public int getDateFromParamId() {
        return dateFromParamId;
    }

    public int getDateToParamId() {
        return dateToParamId;
    }

    public int getAddressParamId() {
        return addressParamId;
    }
}
