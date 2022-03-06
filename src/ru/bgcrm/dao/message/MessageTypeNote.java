package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.bgerp.plugin.kernel.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.struts.action.FileAction.FileInfo;
import ru.bgcrm.struts.action.FileAction.SessionTemporaryFiles;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;
import ru.bgerp.l10n.Localization;

public class MessageTypeNote extends MessageType {
    private static final Log log = Log.getLog();

    public MessageTypeNote(Setup setup, int id, ParameterMap config) throws BGException {
        super(setup, id, config.get("title"), config);
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws Exception {
        message.setSystemId("");
        message.setFrom("");
        message.setProcessed(true);
        message.setToTime(new Date());
        message.setDirection(Message.DIRECTION_INCOMING);

        Map<Integer, FileInfo> tmpFiles = processMessageAttaches(con, form, message);

        new MessageDAO(con).updateMessage(message);

        SessionTemporaryFiles.deleteFiles(form, tmpFiles.keySet());

        // генерация события

        int processId = message.getProcessId();

        // определение кода процесса
        if (processId > 0) {
            Process process = new ProcessDAO(con).getProcess(processId);

            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            if (type == null) {
                log.error("Not found process type with id:" + process.getTypeId());
            } else {
                EventProcessor.processEvent(new ProcessMessageAddedEvent(form, message, process), new SingleConnectionSet(con));
            }
        }
    }

    @Override
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws Exception {
        for (String messageId : messageIds)
            new MessageDAO(conSet.getConnection()).deleteMessage(Utils.parseInt(messageId));
    }

    @Override
    public String getMessageDescription(String lang, Message message) {
        var l = Localization.getLocalizer(Plugin.ID, lang);

        var result = new StringBuilder(200);
        result
            .append(getTitle())
            .append(": \"")
            .append(message.getSubject())
            .append("\"; ")
            .append(l.l("создано: "))
            .append(TimeUtils.format(message.getFromTime(), TimeUtils.FORMAT_TYPE_YMDHM));

        return result.toString();
    }

    @Override
    public boolean isAnswerSupport() {
        return true;
    }

    @Override
    public boolean isAttachmentSupport() {
        return true;
    }

    @Override
    public boolean isEditable(Message message) {
        return true;
    }

    @Override
    public boolean isRemovable(Message message) {
        return true;
    }

    @Override
    public boolean isProcessChangeSupport() {
        return true;
    }

    @Override
    public String getProcessMessageHeaderColor(Message message) {
        return "#e6fb9d";
    }
}
