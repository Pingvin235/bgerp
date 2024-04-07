package org.bgerp.plugin.bil.invoice.event;

import org.bgerp.event.base.UserEvent;
import org.bgerp.plugin.bil.invoice.model.Invoice;

import ru.bgcrm.struts.form.DynActionForm;

public class InvoiceChangedEvent extends UserEvent {
    public static enum Mode {
        CREATED, CHANGED
    }

    private final Invoice invoice;
    private final Mode changeMode;

    public InvoiceChangedEvent(DynActionForm form, Invoice invoice, Mode changeMode) {
        super(form);
        this.invoice = invoice;
        this.changeMode = changeMode;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public boolean isCreated() {
        return changeMode == Mode.CREATED;
    }

    public boolean isChanged() {
        return changeMode == Mode.CHANGED;
    }
}
