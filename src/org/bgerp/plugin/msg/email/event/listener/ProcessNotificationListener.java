package org.bgerp.plugin.msg.email.event.listener;

import java.sql.SQLException;
import java.util.Set;

import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.app.servlet.Interface;
import org.bgerp.dao.message.MessageSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.plugin.msg.email.Plugin;
import org.bgerp.plugin.msg.email.config.ProcessNotificationConfig;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessNotificationListener {
    private static final Log log = Log.getLog();

    public ProcessNotificationListener() {
        EventProcessor.subscribe(this::messageAdded, ProcessMessageAddedEvent.class);
        EventProcessor.subscribe(this::executorsChanged, ProcessChangedEvent.class);
    }

    private void messageAdded(ProcessMessageAddedEvent e, ConnectionSet conSet) {
        Process process = e.getProcess();

        var config = config(process.getTypeId());
        if (config == null)
            return;

        try {
            var l = localizer(e.getForm());
            var text = new StringBuilder(500).append(l.l("email.notification.message", e.getMessage().getText(),
                    Interface.getUrlUser() + "/process#" + process.getId()));

            String subject = subject(process, e.getMessage().getId());

            new org.bgerp.plugin.msg.email.ExpressionObject(process, e.getForm(), conSet.getSlaveConnection())
                    .sendMessageToExecutors(config.userEmailParamId(), subject, text.toString());
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    private void executorsChanged(ProcessChangedEvent e, ConnectionSet conSet) {
        if (!e.isExecutors() && !e.isStatus())
            return;

        Process process = e.getProcess();

        var config = config(process.getTypeId());
        if (config == null)
            return;

        try {
            var l = localizer(e.getForm());
            var text = new StringBuilder(2000)
                    .append(l.l(e.isExecutors() ? "email.notification.executors" : "email.notification.status",
                            Interface.getUrlUser() + "/process#" + process.getId()));

            int messageId = messages(conSet, process, l, text);
            String subject = subject(process, messageId);

            new org.bgerp.plugin.msg.email.ExpressionObject(process, e.getForm(), conSet.getSlaveConnection())
                    .sendMessageToExecutors(config.userEmailParamId(), subject, text.toString());
        } catch (Exception ex) {
            log.error(ex);
        }
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
            .orderFromTimeReverse(true)
            .search(messages);

        int messageId = -1;

        var list = messages.getList();
        if (!list.isEmpty()) {
            messageId = Utils.getFirst(list).getId();

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
            " [" + (process.getDescription().length() < 30 ? process.getDescription() : process.getDescription().substring(0, 30) + "..") + "] ";

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
