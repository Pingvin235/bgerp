package org.bgerp.plugin.bil.subscription.event.listener;

import org.bgerp.plugin.bil.invoice.Plugin;
import org.bgerp.plugin.bil.invoice.event.InvoicePaidEvent;

import ru.bgcrm.event.EventProcessor;

public class PaidInvoicesListener {
    public PaidInvoicesListener() {
        EventProcessor.subscribe((e, conSet) -> {
            // TODO: Move subscription date.
        }, InvoicePaidEvent.class);
    }
}
