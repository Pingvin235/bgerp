package org.bgerp.plugin.bil.billing.subscription;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import org.bgerp.plugin.bil.billing.invoice.model.Position;
import org.bgerp.plugin.bil.billing.invoice.model.PositionProvider;
import org.bgerp.plugin.bil.billing.subscription.dao.SubscriptionDAO;
import org.bgerp.plugin.bil.billing.subscription.model.Subscription;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.util.Setup;

public class InvoicePositionProvider implements PositionProvider {
    private static final Log log = Log.getLog();

    @Override
    public List<Position> getPositions(int processId, YearMonth month) {
        List<Position> result = Collections.emptyList();

        var setup = Setup.getSetup();
        var config = setup.getConfig(Config.class);

        try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
            var process = new ProcessDAO(con).getProcess(processId);
            if (process == null)
                throw new NotFoundException("Process not found: " + processId);

            Subscription subscription = config.getSubscriptions().stream()
                    .filter(s -> s.getProcessTypeId() == process.getTypeId()).findFirst().orElse(null);
            if (subscription == null)
                return result;

            result = new SubscriptionDAO(con).getInvoicePositions(config, subscription, processId);
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }

}
