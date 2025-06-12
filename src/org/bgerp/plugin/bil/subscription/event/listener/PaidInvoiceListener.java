package org.bgerp.plugin.bil.subscription.event.listener;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.dao.expression.Expression;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.model.file.FileData;
import org.bgerp.model.msg.Message;
import org.bgerp.plugin.bil.invoice.event.InvoicePaidEvent;
import org.bgerp.plugin.bil.invoice.model.Invoice;
import org.bgerp.plugin.bil.subscription.Config;
import org.bgerp.plugin.bil.subscription.model.config.PaidInvoiceConfig;
import org.bgerp.util.Log;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class PaidInvoiceListener {
    private static final Log log = Log.getLog();

    public PaidInvoiceListener() {
        EventProcessor.subscribe(this::invoicePaid, InvoicePaidEvent.class);
    }

    private void invoicePaid(InvoicePaidEvent e, ConnectionSet conSet) throws Exception {
        var config = Setup.getSetup().getConfig(Config.class);

        var invoice = e.getInvoice();

        log.debug("Invoice paid: {}, process: {}", invoice.getNumber(), invoice.getProcessId());

        var dao = new ParamValueDAO(conSet.getSlaveConnection());

        Integer subscriptionId = Utils.getFirst(dao.getParamList(invoice.getProcessId(), config.getParamSubscriptionId()));
        if (subscriptionId == null) {
            log.debug("No subscription param value defined.");
            return;
        }

        var subscription = config.getSubscriptionOrThrow(subscriptionId);

        var onPaidConfig = subscription.getPaidInvoiceConfig();
        if (onPaidConfig == null) {
            log.debug("No onPaidInvoiceConfig defined.");
            return;
        }

        var fd = updateLic(e.getForm(), conSet, invoice, config, onPaidConfig);
        if (fd != null)
            createEmail(e.getForm(), conSet, invoice, config, onPaidConfig, fd);
    }

    private FileData updateLic(DynActionForm form, ConnectionSet conSet, Invoice invoice, Config config, PaidInvoiceConfig onPaidConfig) throws Exception {
        LocalDate dateTo = (LocalDate) new Expression(Map.of(
            "invoice", invoice
        )).execute(onPaidConfig.getDateToExpression());

        log.debug("dateTo: {}", dateTo);

        new ParamValueDAO(conSet.getConnection(), true, form.getUserId()).updateParamDate(invoice.getProcessId(), config.getParamDateToId(), TimeConvert.toDate(dateTo));

        return config.updateLic(invoice.getProcessId(), conSet);
    }

    private void createEmail(DynActionForm form, ConnectionSet conSet, Invoice invoice, Config config, PaidInvoiceConfig onPaidConfig, FileData fd) throws Exception {
        var email = Utils.getFirst(new ParamValueDAO(conSet.getSlaveConnection()).getParamEmail(invoice.getProcessId(), config.getParamEmailId()).values());
        if (email == null) {
            log.debug("No email param value defined.");
            return;
        }

        var messageType = onPaidConfig.getEmailMessageType();
        if (messageType == null) {
            log.debug("No email message type defined.");
            return;
        }

        var message = new Message();
        message.setSystemId("");
        message.setProcessId(invoice.getProcessId());
        message.setTypeId(messageType.getId());
        message.setDirection(Message.DIRECTION_OUTGOING);
        message.setUserId(form.getUserId());
        message.setFrom(messageType.getEmail());
        message.setFromTime(new Date());
        message.setTo(email.getValue());
        message.setSubject(onPaidConfig.getEmailSubject());
        message.setText((String) new Expression(Map.of(
            "invoice", invoice
        )).execute(onPaidConfig.getEmailTextExpression()));

        message.addAttach(fd);

        new MessageDAO(conSet.getConnection()).updateMessage(message);
    }
}
