package org.bgerp.plugin.msg.email.config;

import java.util.Collections;
import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.util.Utils;

public class RecipientsConfig extends Config {
    /** Process parameters. */
    public static final String AREA_PROCESS = "process";
    /** All not disabled users. */
    public static final String AREA_USERS = "users";
    /** User executors for a process. */
    public static final String AREA_EXECUTORS = "executors";
    /** Customers linked to a process. */
    public static final String AREA_PROCESS_CUSTOMERS = "process_customers";

    private final Set<String> areas;

    protected RecipientsConfig(ConfigMap config) {
        super(null);
        areas = Collections.unmodifiableSet(Utils.toSet(config.get("email:recipients.search.area",
                AREA_PROCESS + "," + AREA_EXECUTORS + "," + AREA_PROCESS_CUSTOMERS)));
    }

    public Set<String> areas() {
        return areas;
    }
}