package ru.bgcrm.plugin.asterisk;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.model.msg.Message;
import org.bgerp.util.Log;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.MessageTypeCall.CallRegistration;
import ru.bgcrm.struts.form.DynActionForm;

public class AmiEventListener extends Thread implements ManagerEventListener {
    private static final Log log = Log.getLog();

    private final MessageTypeCall messageType;
    private final ConfigMap config;

    private ManagerConnection managerConnection;
    private volatile boolean run = true;

    public AmiEventListener(MessageTypeCall messageType, ConfigMap config) {
        this.messageType = messageType;
        this.config = config;

        start();
    }

    @Override
    public void run() {
        while (run) {
            try {
                managerConnection = new ru.bgcrm.plugin.asterisk.ManagerConnection(config);

                managerConnection.addEventListener(this);

                managerConnection.login();
                managerConnection.sendAction(new StatusAction());

                sleep(AMIManager.CONNECT_TIMEOUT);

                managerConnection.removeEventListener(this);
                if (managerConnection.getState() != ManagerConnectionState.DISCONNECTED) {
                    managerConnection.logoff();
                }
            } catch (Exception e) {
                log.error(e);

                try {
                    sleep(AMIManager.RECONNECT_TIMEOUT);
                } catch (Exception ex) {
                    log.error(e);
                }
            }
        }
    }

    public void logoff() {
        managerConnection.removeEventListener(this);
        managerConnection.logoff();
        run = false;
    }

    @Override
    public void onManagerEvent(ManagerEvent e) {
        log.debug("AMI event: {}", e);

        if (!(e instanceof NewStateEvent))
            return;

        NewStateEvent event = (NewStateEvent) e;

        if (!"Up".equals(event.getChannelStateDesc()))
            return;

        String numberFrom = event.getConnectedLineNum();
        String numberTo = event.getCallerIdNum();
        boolean registerBecauseExpression = false;
        if (messageType.getCheckExpressionCallStore() != null) {
            Map<String, Object> context = new HashMap<>();
            context.put(numberFrom, numberFrom);
            context.put(numberTo, numberTo);
            registerBecauseExpression = new Expression(context).executeCheck(messageType.getCheckExpressionCallStore());
        }

        CallRegistration reg = messageType.getRegistrationByNumber(numberTo);
        // приходят 3 события о вызове, поэтому блокировка по первому путём установки messageForOpenId
        if ((reg != null && reg.getMessageForOpen() == null && numberFrom != null) || registerBecauseExpression) {
            if (reg != null)
                log.info("Call to registered number: {}, event: {}", reg.getNumber(), event);
            else
                log.info("Call because of expression.");

            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                Message message = new Message();
                message.setDirection(Message.DIRECTION_INCOMING);
                message.setTypeId(messageType.getId());
                if (reg != null)
                    message.setUserId(reg.getUserId());
                message.setText("");
                message.setFrom(numberFrom);
                message.setTo(numberTo);
                message.setFromTime(new Date());
                message.setSystemId(event.getUniqueId());

                // по сути там вызывается просто MessageDAO, сделано для единообразия
                messageType.updateMessage(con, DynActionForm.SYSTEM_FORM, message);

                con.commit();

                log.info("Created message: {}", message.getId());

                if (reg != null)
                    reg.setMessageForOpen(message);
            } catch (Exception ex) {
                log.error(ex);
            }
        }
    }
}