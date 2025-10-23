package ru.bgcrm.plugin.asterisk;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.dao.message.call.CallRegistration;
import org.bgerp.model.msg.Message;
import org.bgerp.util.Log;

import ru.bgcrm.dao.message.MessageTypeCall;
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

        if (!(e instanceof NewStateEvent event) || !"Up".equals(event.getChannelStateDesc()))
            return;

        String numberTheir = event.getConnectedLineNum();
        String numberOur = event.getCallerIdNum();

        CallRegistration reg = messageType.getRegistrationByNumber(numberOur);

        if ((reg != null && reg.getMessageForOpen() == null && numberTheir != null)) {
            log.info("Call of registered number: {}, event: {}", reg.getNumber(), event);

            try (var con = Setup.getSetup().getDBConnectionFromPool()) {
                Message message = new Message();
                message.setTypeId(messageType.getId());
                message.setUserId(reg.getUserId());
                message.setText("");
                message.setFromTime(new Date());
                message.setSystemId(event.getUniqueId());

                var outCall = reg.getOutCall();
                if (outCall != null && outCall.isValid()) {
                    message.setDirection(Message.DIRECTION_OUTGOING);
                    message.setFrom(numberOur);
                    message.setTo(numberTheir);
                    if (outCall.getProcessId() > 0)
                        message.setProcessId(outCall.getProcessId());
                    reg.setOutCall(null);
                } else {
                    // property 'exten' contains TO number
                    String exten = event.getExten();
                    if (exten != null && !numberOur.equals(exten)) {
                        log.debug("{} != {}", numberOur, event.getExten());
                        return;
                    }

                    message.setDirection(Message.DIRECTION_INCOMING);
                    message.setFrom(numberTheir);
                    message.setTo(numberOur);
                }

                updateMessage(con, message);

                log.info("Created {} message: {}", message.isIncoming() ? "IN" : "OUT", message.getId());

                // there are might be multiple events for a single call, for preventing multi-processing the first message is set here
                reg.setMessageForOpen(message);
            } catch (Exception ex) {
                log.error(ex);
            }
        } else
            log.debug("No registered number found, the call was already processed or there is no FROM number in the event");
    }

    private void updateMessage(Connection con, Message message) throws SQLException {
        // only for unification, there is MessageDAO called inside
        messageType.updateMessage(con, DynActionForm.SYSTEM_FORM, message);

        con.commit();
    }
}