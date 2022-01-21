package org.bgerp.plugin.bil.billing.invoice.pos;

/*
import org.bgerp.plugin.bil.billing.invoice.model.Invoice;
import org.bgerp.plugin.bil.billing.subscription.Config;
import org.bgerp.plugin.bil.billing.subscription.dao.SubscriptionDAO;
import org.bgerp.plugin.bil.billing.subscription.model.Subscription;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;

public class SubscriptionPositionProvider extends PositionProvider {
    private static final Log log = Log.getLog();

    protected SubscriptionPositionProvider(ParameterMap config) {
        super(null);
    }

    @Override
    public void addPositions(Invoice invoice) {
        int processId = invoice.getProcessId();

        var setup = Setup.getSetup();
        var config = setup.getConfig(Config.class);

        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            var process = new ProcessDAO(con).getProcess(processId);
            if (process == null)
                throw new NotFoundException("Process not found: " + processId);

            Subscription subscription = config.getSubscriptions().stream()
                    .filter(s -> s.getProcessTypeId() == process.getTypeId()).findFirst().orElse(null);
            if (subscription == null)
                return;

            var positions = new SubscriptionDAO(con).getInvoicePositions(config, subscription, processId);
            invoice.getPositions().addAll(positions);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
*/