package org.bgerp.plugin.bil.invoice.event;

import org.bgerp.event.base.UserEvent;
import org.bgerp.plugin.bil.invoice.model.Invoice;

import ru.bgcrm.struts.form.DynActionForm;

public class InvoicePaidEvent extends UserEvent {
    private final Invoice invoice;

    public InvoicePaidEvent(DynActionForm form, Invoice invoice) {
        super(form);
        this.invoice = invoice;
    }

    public Invoice getInvoice() {
        return invoice;
    }
}
