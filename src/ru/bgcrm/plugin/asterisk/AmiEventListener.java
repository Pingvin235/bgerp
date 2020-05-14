package ru.bgcrm.plugin.asterisk;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewStateEvent;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.dao.message.MessageTypeCall;
import ru.bgcrm.dao.message.MessageTypeCall.CallRegistration;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class AmiEventListener extends Thread implements ManagerEventListener {
    private final MessageTypeCall messageType;
    private final ParameterMap config;

    private ManagerConnection managerConnection;
    private volatile boolean run = true;

    public AmiEventListener(MessageTypeCall messageType, ParameterMap config) throws BGException {
        this.messageType = messageType;
        this.config = config;

        start();
    }

    @Override
    public void run() {
        while (run) {
            try {
                String host = config.get("host");
                int port = config.getInt("port", 5038);
                String login = config.get("login");
                String pswd = config.get("pswd");

                AMIManager.log.debug("Connecting AMI host: " + host + "; port: " + port + "; login: " + login + "; pswd: " + pswd);

                managerConnection = new ManagerConnectionFactory(host, login, pswd).createManagerConnection();

                managerConnection.addEventListener(this);

                managerConnection.login();
                managerConnection.sendAction(new StatusAction());

                sleep(AMIManager.CONNECT_TIMEOUT);

                managerConnection.removeEventListener(this);
                if (managerConnection.getState() != ManagerConnectionState.DISCONNECTED) {
                    managerConnection.logoff();
                }
            } catch (Exception e) {
                AMIManager.log.error(e.getMessage(), e);

                try {
                    sleep(AMIManager.RECONNECT_TIMEOUT);
                } catch (Exception ex) {
                    AMIManager.log.error(e.getMessage(), e);
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
        if (AMIManager.log.isDebugEnabled())
            AMIManager.log.debug("AMI event: " + e);

        /*04-01/18:45:37  INFO [Asterisk-Java ManagerConnection-2-Reader-0] AMIManager - AMI event: org.asteriskjava.manager.event.BridgeEvent[dateReceived='Tue Apr 01 18:45:37 YEKT 2014',
        privilege='call,all',uniqueid1='1396356317.899',uniqueid2='1396356318.902',sequencenumber=null,link='true',bridgestate='Link',
        bridgetype='core',unlink='false',timestamp=null,channel1='Local/1005@from-queue-00000016;2',channel2='SIP/1005-0000034e',server=null,
        callerid1='3472924823',callerid2='1005',systemHashcode=2631472]*/

        /*Event: Newstate
        Privilege: call,all
        Channel: Local/904@from-queue-00000621;1
        ChannelState: 6
        ChannelStateDesc: Up
        CallerIDNum: 904
        CallerIDName: 904
        ConnectedLineNum: 89075270744
        ConnectedLineName: 89075270744
        Uniqueid: 1406622918.6497 */

        if (!(e instanceof NewStateEvent))
            return;
        
        NewStateEvent event = (NewStateEvent) e;

        if (!"Up".equals(event.getChannelStateDesc()))
            return;

        String numberFrom = event.getConnectedlinenum();
        String numberTo = event.getCallerIdNum();
        boolean registerBecauseExpression = false;
        if (messageType.getCheckExpressionCallStore() != null) {
            Map<String, Object> context = new HashMap<>();
            context.put(numberFrom, numberFrom);
            context.put(numberTo, numberTo);
            registerBecauseExpression = new Expression(context).check(messageType.getCheckExpressionCallStore());
        }

        CallRegistration reg = messageType.getRegistrationByNumber(numberTo);
        // приходят 3 события о вызове, поэтому блокировка по первому путём установки messageForOpenId
        if ((reg != null && reg.getMessageForOpenId() == null) || registerBecauseExpression) {
            if (reg != null)
                AMIManager.log.info("Call to registered number: " + reg.getNumber());
            else
                AMIManager.log.info("Call because of expression.");

            Connection con = Setup.getSetup().getDBConnectionFromPool();
            try {
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
                messageType.updateMessage(con, DynActionForm.SERVER_FORM, message);

                con.commit();

                AMIManager.log.info("Created message: " + message.getId());

                if (reg != null)
                    reg.setMessageForOpenId(message.getId());
            } catch (Exception ex) {
                AMIManager.log.error(ex.getMessage(), ex);
            } finally {
                SQLUtils.closeConnection(con);
            }
        }
    }
}