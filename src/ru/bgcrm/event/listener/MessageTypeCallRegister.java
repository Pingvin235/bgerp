package ru.bgcrm.event.listener;

import java.sql.Connection;
import java.util.Date;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGException;
import org.bgerp.dao.message.call.CallRegistration;
import org.bgerp.model.msg.Message;
import org.bgerp.model.msg.config.MessageTypeConfig;
import org.bgerp.util.Log;

import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.event.RunClassRequestEvent;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SQLUtils;

/**
 * Обработчик событий для регистрации звонка внешним HTTP запросом.
 * В случае если на номер <b>to</b> зарегестрирован пользователь - ему будет открыта страница для обработки сообщения.
 * Параметры запроса:<br/>
 * <b>typeId</b> - код типа сообщения;<br/>
 * <b>from</b> - с номера;<br/>
 * <b>to</b> - на номер.<br/>
 * Пример URL:<br/>
 * http://[host]:[port]/admin/run.do?method=runClass&iface=event&class=ru.bgcrm.event.listener.MessageTypeCallRegister&j_username=[user]&j_password=[pswd]&typeId=[typeId]&from=num1&to=num2
 */
public class MessageTypeCallRegister implements EventListener<Event> {
    private static final Log log = Log.getLog();

    public MessageTypeCallRegister() {}

    @Override
    public void notify(Event e, ConnectionSet connectionSet) {
        RunClassRequestEvent event = (RunClassRequestEvent) e;

        DynActionForm form = event.getForm();

        MessageTypeCall messageType = (MessageTypeCall) Setup.getSetup().getConfig(MessageTypeConfig.class).getTypeMap()
                .get(form.getParamInt("typeId"));
        if (messageType == null) {
            throw new BGException("Не найден тип сообщения.");
        }

        String from = form.getParam("from", "");
        String to = form.getParam("to", "");

        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            CallRegistration reg = messageType.getRegistrationByNumber(to);
            if (reg != null)
                log.info("Call to registered number: {}", reg.getNumber());

            Message message = new Message();
            message.setDirection(Message.DIRECTION_INCOMING);
            message.setTypeId(messageType.getId());
            message.setUserId(reg != null ? reg.getUserId() : 0);
            message.setText("");
            message.setFrom(from);
            message.setTo(to);
            message.setFromTime(new Date());
            message.setSystemId(messageType.getId() + ":" + System.currentTimeMillis());

            // по сути там вызывается просто MessageDAO, сделано для единообразия
            messageType.updateMessage(con, DynActionForm.SYSTEM_FORM, message);

            con.commit();

            log.info("Created message: {}", message.getId());

            if (reg != null)
                reg.setMessageForOpen(message);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            SQLUtils.closeConnection(con);
        }
    }
}