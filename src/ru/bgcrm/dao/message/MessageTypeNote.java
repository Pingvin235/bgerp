package ru.bgcrm.dao.message;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.cfg.bean.annotation.Bean;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.l10n.Localization;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.model.file.tmp.FileInfo;
import org.bgerp.model.file.tmp.SessionTemporaryFiles;
import org.bgerp.model.msg.Message;
import org.bgerp.plugin.kernel.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.process.ProcessMessageAddedEvent;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Bean
public class MessageTypeNote extends MessageType {
    private static final Log log = Log.getLog();

    private final boolean createUnread;

    public MessageTypeNote(Setup setup, int id, ConfigMap config) {
        super(setup, id, config.get("title"), config);
        createUnread = config.getBoolean("create.unread");
    }

    @Override
    public boolean isAnswerSupport() {
        return true;
    }

    public Message getAnswerMessage(Message original) {
        var result = new Message();
        result.setTypeId(original.getTypeId());
        result.setProcessId(original.getProcessId());

        var subject = Utils.maskNull(original.getSubject());
        subject = subject.startsWith("Re:") ? subject : "Re: " + subject;
        result.setSubject(subject);

        result.setText(answerText(original.getText()));
        result.setTo(original.getFrom());

        return result;
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
    public boolean isReadable() {
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

    @Override
    public String getEditorJsp() {
        return Plugin.ENDPOINT_MESSAGE_EDITOR;
    }

    @Override
    public String getMessageDescription(String lang, Message message) {
        var l = Localization.getLocalizer(lang, Plugin.ID);

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
    public void messageDelete(ConnectionSet conSet, String... messageIds) throws Exception {
        for (String messageId : messageIds)
            new MessageDAO(conSet.getConnection()).deleteMessage(Utils.parseInt(messageId));
    }

    @Override
    public void updateMessage(Connection con, DynActionForm form, Message message) throws Exception {
        message.setSystemId("");
        message.setFrom("");
        if (!createUnread)
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

}
