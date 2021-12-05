package org.bgerp.plugin.bil.billing.invoice.event.listener;

import org.bgerp.event.ProcessFileGetEvent;
import org.bgerp.event.ProcessFilesEvent;
import org.bgerp.plugin.bil.billing.invoice.Config;
import org.bgerp.plugin.bil.billing.invoice.Plugin;
import org.bgerp.plugin.bil.billing.invoice.dao.InvoiceDAO;
import org.bgerp.plugin.bil.billing.invoice.dao.InvoiceSearchDAO;
import org.bgerp.plugin.bil.billing.invoice.model.Invoice;
import org.bgerp.util.Log;

import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgerp.l10n.Localization;

/**
 * Provides files with print forms for adding to messages.
 *
 * @author Shamil Vakhitov
 */
public class Files {
    private static final Log log = Log.getLog();

    private static final String PREFIX = Plugin.ID + ":";

    public Files() {
        EventProcessor.subscribe((e, conSet) -> {
            var result = new SearchResult<Invoice>();
            new InvoiceSearchDAO(conSet.getSlaveConnection())
                .withProcessId(e.getProcessId())
                .withPayed(false)
                .orderFromDate()
                .orderDesc()
                .search(result);

            var l = Localization.getLocalizer(Plugin.ID, e.getForm().getHttpRequest());

            for (var invoice : result.getList()) {
                e.addAnnouncedFile(new IdStringTitle(PREFIX + invoice.getId(), l.l("Счёт") + " " + invoice.getNumber()));
            }
        }, ProcessFilesEvent.class);

        EventProcessor.subscribe((e, conSet) -> {
            if (!e.getFileId().startsWith(PREFIX))
                return;

            int id = Utils.parseInt(e.getFileId().substring(PREFIX.length()));
            var invoice = new InvoiceDAO(conSet.getSlaveConnection()).get(id);

            if (invoice == null) {
                log.error("Not found invoice with ID: {}", id);
                return;
            }

            var type = Setup.getSetup().getConfig(Config.class).getType(invoice.getTypeId());

            e.setFile(invoice.getNumber() + ".html", type.doc(e.getForm(), invoice));
        }, ProcessFileGetEvent.class);
    }
}
