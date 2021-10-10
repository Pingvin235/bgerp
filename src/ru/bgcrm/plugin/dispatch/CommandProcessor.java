package ru.bgcrm.plugin.dispatch;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.util.Log;

import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.model.Dispatch;
import ru.bgcrm.util.MailConfig;
import ru.bgcrm.util.MailMsg;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class CommandProcessor implements Runnable {
    private static final Log log = Log.getLog();

    private static final FetchProfile FETCH_PROFILE = new FetchProfile();
    static {
        FETCH_PROFILE.add(FetchProfile.Item.ENVELOPE);
        FETCH_PROFILE.add("To");
        FETCH_PROFILE.add("CC");
        FETCH_PROFILE.add("Message-ID");
        FETCH_PROFILE.add("Received");
    }

    @Override
    public void run() {
        try {
            final Setup setup = Setup.getSetup();

            Config config = setup.getConfig(Config.class);

            MailConfig mailConfig = config.getMailConfig();
            if (!mailConfig.check()) {
                log.error("Mail config is not configured!");
                return;
            }

            Session session = mailConfig.getSmtpSession(Setup.getSetup());

            Store store = mailConfig.getImapStore();
            try {
                Folder incomingFolder = store.getFolder("INBOX");
                incomingFolder.open(Folder.READ_WRITE);

                javax.mail.Message[] messages = incomingFolder.getMessages();

                incomingFolder.fetch(messages, FETCH_PROFILE);

                for (javax.mail.Message message : messages) {
                    try {
                        String from = ((InternetAddress) message.getFrom()[0]).getAddress();
                        String subject = message.getSubject();
                        String text = (String) message.getContent();

                        log.info("Processing: " + message.getSubject() + "; from: " + from);

                        if ("STATE".equals(subject)) {
                            Connection conSlave = setup.getConnectionPool().getDBSlaveConnectionFromPool();
                            try {
                                sendDispatchStateList(conSlave, config, session, from);
                            } finally {
                                SQLUtils.closeConnection(conSlave);
                            }
                        } else if (subject.endsWith("SUBSCRIBE")) {
                            Set<Integer> deltaIds = Utils.toIntegerSet(StringUtils.substringBefore(text, "\n").trim());

                            Connection con = setup.getDBConnectionFromPool();
                            try {
                                DispatchDAO dispatchDao = new DispatchDAO(con);

                                Set<Integer> dispatchIds = dispatchDao.accountSubsriptionList(from).stream()
                                        .map(Dispatch::getId).collect(Collectors.toSet());

                                if (subject.startsWith("UN"))
                                    dispatchIds.removeAll(deltaIds);
                                else
                                    dispatchIds.addAll(deltaIds);

                                dispatchDao.accountSubsriptionUpdate(from, dispatchIds);

                                sendDispatchStateList(con, config, session, from);

                                con.commit();
                            } finally {
                                SQLUtils.closeConnection(con);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    message.setFlag(Flags.Flag.DELETED, true);
                }

                incomingFolder.close(true);
            } finally {
                store.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void sendDispatchStateList(Connection con, Config config, Session session, String email)
            throws Exception {
        DispatchDAO dispatchDao = new DispatchDAO(con);
        List<Dispatch> dispatchList = dispatchDao.dispatchList(null);
        Set<Integer> subscriptions = dispatchDao.accountSubsriptionList(email).stream()
                .map(Dispatch::getId).collect(Collectors.toSet());

        String encoding = MailMsg.getParamMailEncoding(Setup.getSetup());
        Transport transport = null;

        try {
            transport = session.getTransport();
            transport.connect();

            StringBuilder body = new StringBuilder(200);
            for (Dispatch dispatch : dispatchList) {
                body.append("<html><body><div>");
                body.append(dispatch.getTitle());
                body.append(" ");
                if (subscriptions.contains(dispatch.getId()))
                    body.append("<b><a href=\"mailto:" + config.getMailConfig().getFrom() + "?subject=UNSUBSCRIBE&body=" + dispatch.getId() + "\">отписаться</a></b>");
                else
                    body.append("<a href=\"mailto:" + config.getMailConfig().getFrom() + "?subject=SUBSCRIBE&body=" + dispatch.getId() + "\">подписаться</a>");
                body.append("</div></body></html>");
            }

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(config.getMailConfig().getFrom()));
            msg.setSubject("Статус подписок", encoding);
            msg.setContent(body.toString(),  "text/html;charset=" + encoding);
            msg.setRecipients(RecipientType.TO, email);

            transport.sendMessage(msg, msg.getAllRecipients());
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }
}
