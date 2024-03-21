package ru.bgcrm.plugin.bgbilling.creator;

import java.sql.Connection;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exec.scheduler.Task;
import org.bgerp.util.Log;

/**
 * Задача планировщика импорта контрагента с серверов биллингов.
 */
@Bean
public class CustomerCreator extends Task {
    private static final Log log = Log.getLog();

    public CustomerCreator() {
        super(null);
    }

    @Override
    public String getTitle() {
        return "BGBilling импорт контрагентов";
    }

    @Override
    public void run() {
        Setup setup = Setup.getSetup();

        try (Connection con = setup.getDBConnectionFromPool()) {
            Config config = setup.getConfig(Config.class);
            for (ServerCustomerCreator c : config.serverCreatorList) {
                if (config.importBillingIds.size() != 0 && !config.importBillingIds.contains(c.getBillingId())) {
                    log.info("Skipping import: {}", c.getBillingId());
                    continue;
                }
                c.createCustomers(con);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}