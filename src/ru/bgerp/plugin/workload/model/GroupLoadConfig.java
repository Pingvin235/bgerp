package ru.bgerp.plugin.workload.model;

import java.util.Collections;
import java.util.Set;

import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgerp.plugin.workload.Plugin;

public class GroupLoadConfig extends Config {

    private final boolean enabled;
    private final Set<Integer> userGroupIds;
    private final Set<Integer> processTypeIds;

    private final int dateFromParamId;
    private final int dateToParamId;

    private final int addressParamId;

    protected GroupLoadConfig(ParameterMap setup, boolean validate) {
        super(setup, validate);
        String prefix = Plugin.ID + ":groupLoad.";
        this.userGroupIds = Collections.unmodifiableSet(Utils.toIntegerSet(setup.get(prefix + "userGroupIds")));
        this.processTypeIds = Collections.unmodifiableSet(Utils.toIntegerSet(setup.get(prefix + "processTypeIds")));
        this.enabled = !userGroupIds.isEmpty() || !processTypeIds.isEmpty();

        this.dateFromParamId = setup.getInt(prefix + "dateFromParamId");
        this.dateToParamId = setup.getInt(prefix + "dateToParamId");

        this.addressParamId = setup.getInt(prefix + "addressParamId");
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
