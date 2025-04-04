package org.bgerp.plugin.msg.email.event.listener;

import java.sql.SQLException;
import java.util.Set;

import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.app.servlet.Interface;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.message.MessageSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.msg.Message;
import org.bgerp.plugin.bil.invoice.event.InvoicePaidEvent;
import org.bgerp.plugin.msg.email.ExpressionObject;
import org.bgerp.plugin.msg.email.Plugin;
import org.bgerp.plugin.msg.email.config.ProcessNotificationConfig;
import org.bgerp.plugin.pln.agree.event.AgreementEvent;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessNotificationListener {
    private static final Log log = Log.getLog();

    public ProcessNotificationListener() {
        EventProcessor.subscribe(this::messageAdded, ProcessMessageAddedEvent.class);
        EventProcessor.subscribe(this::processChanged, ProcessChangedEvent.class);
        EventProcessor.subscribe(this::invoicePaid, InvoicePaidEvent.class);
        EventProcessor.subscribe(this::agreement, AgreementEvent.class);
    }

    private void messageAdded(ProcessMessageAddedEvent e, ConnectionSet conSet) throws Exception {
        Process process = e.getProcess();

        var config = config(process.getTypeId());
        if (config == null)
            return;

        var l = localizer(e.getForm());
        var text = new StringBuilder(500)
                .append(l.l("email.notification.message", e.getMessage().getText(), Interface.getUrlUser() + "/process#" + process.getId()));

        // may be add here history of incoming messages
        String subject = l.l("New message in process") + " " + subject(process, e.getMessage().getId());

        new ExpressionObject(process, e.getForm(), conSet.getSlaveConnection())
                .sendMessageToExecutors(config.userEmailParamId(), subject, text.toString());
    }

    private void processChanged(ProcessChangedEvent e, ConnectionSet conSet) throws Exception {
        if (!e.isExecutors() && !e.isStatus())
            return;

        Process process = e.getProcess();

        var config = config(process.getTypeId());
        if (config == null)
            return;

        var l = localizer(e.getForm());

        var text = new StringBuilder(2000)
                .append(l.l(e.isExecutors() ? "email.notification.executors" : "email.notification.status",
                        Interface.getUrlUser() + "/process#" + process.getId()));

        int messageId = messages(conSet, process, l, text);
        String subject =
            l.l(e.isExecutors() ? "Process executors changed" : "Process status changed") +
            " " + subject(process, messageId);

        new ExpressionObject(process, e.getForm(), conSet.getSlaveConnection())
                .sendMessageToExecutors(config.userEmailParamId(), subject, text.toString());
    }

    private void invoicePaid(InvoicePaidEvent e, ConnectionSet conSet) throws Exception {
        var invoice = e.getInvoice();

        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcessOrThrow(invoice.getProcessId());

        var config = config(process.getTypeId());
        if (config == null)
            return;

        var customer = Utils.getFirst(new ProcessLinkDAO(conSet.getSlaveConnection(), e.getForm()).getLinkCustomers(process.getId(), null));
        String customerTitle = customer != null ? customer.getTitle() : "???";

        var l = localizer(e.getForm());
        String text = l.l("email.notification.invoice.paid",
            customerTitle,
            invoice.monthsPeriod(Localization.getLang(e.getForm().getHttpRequest())),
            Utils.format(invoice.getAmount()),
            TimeUtils.format(invoice.getCreateTime(), TimeUtils.FORMAT_TYPE_YMD),
            Interface.getUrlUser() + "/process#" + process.getId());

        String subject = l.l("Paid invoice {}", invoice.getNumber());

        new ExpressionObject(process, e.getForm(), conSet.getSlaveConnection()).sendMessageToExecutors(config.userEmailParamId(), subject, text);
    }

    private void agreement(AgreementEvent e, ConnectionSet conSet) throws Exception {
        var process = e.getProcess();

        var config = config(process.getTypeId());
        if (config == null)
            return;

        String subject = null, text = null;

        var l = localizer(e.getForm());

        String userTitle = e.getForm().getUser().getTitle();

        switch (e.getMode()) {
            case START -> {
                subject = l.l("Agreement has started") + " " + subject(process, 0);
                text = l.l("agree.notification.start", userTitle);
            }
            case PROGRESS -> {
                subject = l.l("Agreement in progress") + " " + subject(process, 0);
                text = l.l("agree.notification.progress", userTitle);
            }
            case FINISH -> {
                subject = l.l("Agreement was finished") + " " + subject(process, 0);
                text = l.l("agree.notification.finish", userTitle);
            }
        }

        new ExpressionObject(process, e.getForm(), conSet.getSlaveConnection()).sendMessageToExecutors(config.userEmailParamId(), subject, text);
    }

    private Localizer localizer(DynActionForm form) {
        return Localization.getLocalizer(Plugin.ID, form.getHttpRequest());
    }

    private int messages(ConnectionSet conSet, Process process, Localizer l, StringBuilder text) throws SQLException {
        Pageable<Message> messages = new Pageable<>();
        messages.getPage().setPageIndex(Page.PAGE_INDEX_NO_PAGING);

        new MessageSearchDAO(conSet.getSlaveConnection())
            .withProcessIds(Set.of(process.getId()))
            .withDirection(Message.DIRECTION_INCOMING)
            .order(MessageSearchDAO.Order.FROM_TIME_DESC)
            .search(messages);

        int messageId = -1;

        var list = messages.getList();
        if (!list.isEmpty()) {
            messageId = list.get(0).getId();

            text.append("\n\n");

            for (var m : list) {
                text
                    .append(l.l("Incoming message #"))
                    .append(m.getId())
                    .append("\n")
                    .append("--------------------------")
                    .append("\n")
                    .append(m.getText())
                    .append("\n\n");
            }
        }

        return messageId;
    }

    private String subject(Process process, int messageId) {
        String result = "#" + process.getId() +
            " [" + (process.getDescription().length() < 30 ? process.getDescription() : process.getDescription().substring(0, 30) + "..") + "]";

        if (messageId > 0)
            result += " QA:" + messageId;

        return result;
    }

    private ProcessNotificationConfig config(int typeId) {
        try {
            return ProcessTypeCache.getProcessTypeOrThrow(typeId).getProperties().getConfigMap()
                    .getConfig(ProcessNotificationConfig.class);
        } catch (NotFoundException e) {
            log.error(e);
            return null;
        }
    }
}
