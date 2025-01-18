package org.bgerp.plugin.msg.email.message;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bgerp.action.open.ProcessAction.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.cache.UserCache;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;
import org.bgerp.model.msg.config.TagConfig;
import org.bgerp.model.msg.config.TagConfig.Tag;
import org.bgerp.plugin.msg.email.Plugin;
import org.bgerp.util.Log;
import org.bgerp.util.mail.MailMsg;

import com.google.common.annotations.VisibleForTesting;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Utils;

/**
 * E-Mail content creator.
 *
 * @author Shamil Vakhitov
 */
public class MessageContent {
    private static final Log log = Log.getLog();

    /** Configs source. */
    private final Setup setup;
    private final String encoding;
    private final String signExpression;
    private final boolean signStandard;

    MessageContent(Setup setup, String encoding, ConfigMap config) throws BGMessageException {
        this.setup = setup;
        this.encoding = encoding;
        signExpression = config.getSok("sign.expression", "signExpression");
        signStandard = config.getSokBoolean(true, "sign.standard", "signStandard");
    }

    /**
     * Set E-Mail message content.
     * @param message
     * @param msg
     * @throws Exception
     */
    void create(MimeMessage message, String lang, Message msg) throws Exception {
        var l = Localization.getLocalizer(lang, Plugin.ID);

        var text = new StringBuilder(msg.getText().length() + 400)
            .append(msg.getText())
            .append("\n\n-- ");

        if (Utils.notBlankString(signExpression)) {
            Map<String, Object> context = Map.of(
                User.OBJECT_TYPE, UserCache.getUser(msg.getUserId()),
                "message", msg
            );
            text.append(new Expression(context).executeGetString(signExpression));
        }

        if (signStandard)
            text.append(l.l("email.sign.standard")).append("\n");

        var historyPart = history(text, l, msg);

        var textPart = new MimeBodyPart();
        textPart.setText(text.toString(), encoding);

        var multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        if (historyPart != null)
            multipart.addBodyPart(historyPart);

        if (!msg.getAttachList().isEmpty()) {
            try (var con = setup.getDBSlaveConnectionFromPool()) {
                var fileDao = new FileDataDAO(con);

                for (FileData attach : msg.getAttachList()) {
                    File file = fileDao.getFile(attach);

                    MimeBodyPart attachPart = new MimeBodyPart();
                    MailMsg.setAttachContentTypeHeader(attachPart);
                    attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
                    MailMsg.setAttachFileName(attachPart, attach.getTitle(), encoding);
                    multipart.addBodyPart(attachPart);

                    log.debug("Attach: {}", attach.getTitle());
                }
            }
        }

        message.setContent(multipart);
    }

    /**
     * Appends text with history info and open process link.
     * @param text body text, can be modified during the changes.
     * @param l localizer with target language.
     * @param msg message.
     * @return mime part with 'History.txt' or {@code null} if no history to attach.
     * @throws Exception
     */
    @VisibleForTesting
    protected MimeBodyPart history(StringBuilder text, Localizer l, Message msg) throws Exception {
        int processId = msg.getProcessId();
        if (processId <= 0)
            return null;

        String to = msg.getTo();

        var pair = getHistory(l, text, processId, msg);

        int historyModeTag = pair.getFirst();
        List<Message> messageList = pair.getSecond();

        if (historyModeTag == 0)
            return null;

        text
            .append(l.l("Историю переписки вы можете посмотреть в приложенном файле History.txt"))
            .append("\n");

        var history = new StringBuilder(1000)
            .append(l.l("История сообщений по процессу #{}", processId))
            .append(":\n------------------------------------------");

        var typeConfig = setup.getConfig(MessageTypeConfig.class);

        for (Message historyItem : messageList) {
            if (historyModeTag == Tag.TAG_HISTORY_WITH_ADDRESS_ID
                && !historyItem.getFrom().equals(to)
                && !historyItem.getTo().equals(to)) {
                continue;
            }

            var type = typeConfig.getTypeMap().get(historyItem.getTypeId());
            history
                .append("\n\n")
                .append(type.getMessageDescription(l.getLang(), historyItem))
                .append("\n------------------------------------------\n")
                .append(historyItem.getText());
        }

        var historyPart = new MimeBodyPart();
        historyPart.setText(history.toString(), encoding);
        historyPart.setFileName("History.txt");

        return historyPart;
    }

    @VisibleForTesting
    protected Pair<Integer, List<Message>> getHistory(Localizer l, StringBuilder text, int processId, Message msg) throws Exception {
        int historyModeTag = 0;
        List<Message> messageList = null;

        var tagsConfig = setup.getConfig(TagConfig.class);

        try (var con = setup.getDBSlaveConnectionFromPool()) {
            var dao = new MessageDAO(con);
            historyModeTag = tagsConfig.getSelectedHistoryTag(dao.getMessageTags(msg.getId()));
            if (historyModeTag != 0) {
                messageList = dao.getProcessMessageList(processId, msg.getId());
                if (historyModeTag == Tag.TAG_HISTORY_ALL_ID) {
                    openProcess(l, text, processId);
                }
            }
        }

        return new Pair<>(historyModeTag, messageList);
    }

    private void openProcess(Localizer l, StringBuilder text, int processId) throws SQLException {
        var config = setup.getConfig(Config.class);
        if (config == null)
            return;

        var process = getProcess(processId);
        var url = config.url(process);
        if (Utils.notBlankString(url)) {
            text
                .append(l.l("Карточка процесса: {}", url))
                .append("\n");
        }
    }

    @VisibleForTesting
    protected Process getProcess(int processId) throws SQLException {
        try (var con = setup.getDBConnectionFromPool()) {
            return new ProcessDAO(con).getProcess(processId);
        }
    }
}
