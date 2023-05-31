package ru.bgcrm.plugin.bgbilling.creator;

import java.sql.Connection;

import org.bgerp.util.Log;

import ru.bgcrm.util.Setup;

/**
 * Задача планировщика импорта контрагента с серверов биллингов.
 */
public class CustomerCreator implements Runnable {
    private static final Log log = Log.getLog();

    @Override
    public void run() {
        Setup setup = Setup.getSetup();

        try (Connection con = setup.getDBConnectionFromPool()) {
            Config config = setup.getConfig(Config.class);
            for (ServerCustomerCreator c : config.serverCreatorList) {
                if (config.importBillingIds.size() != 0 && !config.importBillingIds.contains(c.getBillingId())) {
                    log.info("Skipping import: " + c.getBillingId());
                    continue;
                }
                c.createCustomers(con);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}