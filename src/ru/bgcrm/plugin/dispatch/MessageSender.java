package ru.bgcrm.plugin.dispatch;

import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.bgerp.util.Log;

import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.model.DispatchMessage;
import ru.bgcrm.util.MailMsg;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class MessageSender implements Runnable {
    private static final Log log = Log.getLog();

    @Override
    public void run() {
        try {
            final Setup setup = Setup.getSetup();

            Config config = setup.getConfig(Config.class);
            if (!config.getMailConfig().check()) {
                log.error("Mail config is not configured!");
                return;
            }

            List<DispatchMessage> messageList = Collections.emptyList();

            Connection conSlave = setup.getConnectionPool().getDBSlaveConnectionFromPool();
            try {
                messageList = new DispatchDAO(conSlave).messageUnsentList();
            } finally {
                SQLUtils.closeConnection(conSlave);
            }

            for (DispatchMessage message : messageList) {
                List<String> accountList = Collections.emptyList();

                conSlave = setup.getConnectionPool().getDBSlaveConnectionFromPool();
                try {
                    accountList = new DispatchDAO(conSlave).messageAccountList(message.getId());
                } finally {
                    SQLUtils.closeConnection(conSlave);
                }

                log.debug("Found message to send: {}; accounts count: {}", message, accountList.size());

                String encoding = MailMsg.getParamMailEncoding(Setup.getSetup());
                Session session = config.getMailConfig().getSmtpSession(Setup.getSetup());

                Transport transport = null;

                try {
                    transport = session.getTransport();
                    transport.connect();

                    MimeMessage msg = new MimeMessage(session);
                    msg.setFrom(new InternetAddress(config.getMailConfig().getFrom()));
                    msg.setSubject(message.getTitle(), encoding);
                    msg.setText(message.getText(), encoding);

                    for (String account : accountList) {
                        msg.setRecipients(RecipientType.TO, InternetAddress.parse(account));
                        transport.sendMessage(msg, msg.getAllRecipients());
                    }
                } finally {
                    if (transport != null) {
                        transport.close();
                    }
                }

                message.setSentTime(new Date());

                Connection con = setup.getDBConnectionFromPool();
                try {
                    new DispatchDAO(con).messageUpdate(message);
                    con.commit();
                } finally {
                    SQLUtils.closeConnection(con);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
