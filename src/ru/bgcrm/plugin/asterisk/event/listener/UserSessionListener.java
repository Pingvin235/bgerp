package ru.bgcrm.plugin.asterisk.event.listener;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.servlet.user.event.UserSessionCreatedEvent;
import org.bgerp.util.Log;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.util.Utils;

public class UserSessionListener {
    private static final Log log = Log.getLog();

    public UserSessionListener() {
        EventProcessor.subscribe((event, conSet) -> {
            sessionCreated(event);
        }, UserSessionCreatedEvent.class);
    }

    private void sessionCreated(UserSessionCreatedEvent event) {
        int userId = event.getSession().getUser().getId();
        try {
            MessageTypeCall messageType = Setup.getSetup().getConfig(MessageTypeConfig.class).getMessageType(MessageTypeCall.class);
            if (messageType == null || messageType.getRegistrationByUser(userId) != null)
                return;

            String number = messageType.getUserOfferedNumber(userId);
            if (Utils.isBlankString(number))
                return;

            if (messageType.getConfigMap().getBoolean("autoNumberRegister", true))
                messageType.numberRegister(userId, number);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
