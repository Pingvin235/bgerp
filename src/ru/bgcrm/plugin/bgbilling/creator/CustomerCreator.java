package ru.bgcrm.plugin.bgbilling.creator;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bgerp.scheduler.Scheduler;
import org.bgerp.util.Log;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Задача планировщика импорта контрагента с серверов биллингов.
 */
public class CustomerCreator implements Runnable {
    private static final Log log = Log.getLog();

    private static final AtomicBoolean runFlag = new AtomicBoolean(false);

    @Override
    public void run() {
        if (runFlag.get()) {
            log.warn("Customer creator task already running..");
            return;
        }

        runFlag.set(true);

        long time = System.currentTimeMillis();

        Setup setup = Setup.getSetup();

        Connection con = setup.getDBConnectionFromPool();
        try {
            Config config = setup.getConfig(Config.class);
            for (ServerCustomerCreator c : config.serverCreatorList) {
                if (config.importBillingIds.size() != 0 && !config.importBillingIds.contains(c.getBillingId())) {
                    log.info("Skipping import: " + c.getBillingId());
                    continue;
                }
                c.createCustomers(con);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
            runFlag.set(false);
        }

        Scheduler.logExecutingTime(this, time);
    }

    public static void main(String[] args) {
        try {
            new Thread(new CustomerCreator()).start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}