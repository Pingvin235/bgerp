package org.bgerp.plugin.telegram;

import org.bgerp.util.Log;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {
    private static class UserData {
        private String login;
    }

    private static final Log log = Log.getLog();

    private static Bot instance;

    private final Map<String, UserData> userMap = new HashMap<>();

    private static BotSession botSession;

    private Bot() {
    }

    public static Bot getInstance() {
        if (instance == null)
            reinit();

        if (instance == null)
            log.info("In config server not enable bot (telegram:botStart)");

        return instance;
    }

    private static void reinit() {
        log.info("Re init.... telegramBot");
        Config config = Setup.getSetup().getConfig(Config.class);
        if (!config.isBotStart()) {
            log.info("Skipping telegramBot start.");
            return;
        }
        try {
            instance = init();
        } catch (Throwable t) {
            log.error("telegramBot start ERROR", t);
        }
    }

    private static Bot init() throws Exception {

        // trying to stop the old one
        try {
            if (botSession != null) {
                botSession.stop();
                botSession = null;
            }
        } catch (Exception e) {
            log.error("Catch exception", e);
        }

        TelegramBotsApi botapi = new TelegramBotsApi(DefaultBotSession.class);
        DefaultBotOptions botOptions = new DefaultBotOptions();
        Config config = Setup.getSetup().getConfig(Config.class);
        if (config.getProxyHost() != null) {
            botOptions.setProxyHost(config.getProxyHost());
            botOptions.setProxyPort(Utils.parseInt(config.getProxyPort(), -1));
            DefaultBotOptions.ProxyType type = DefaultBotOptions.ProxyType.NO_PROXY;
            if (config.getProxyType().toUpperCase().equals("SOCKS5")) {
                type = DefaultBotOptions.ProxyType.SOCKS5;
            } else if (config.getProxyType().toUpperCase().equals("HTTP")) {
                type = DefaultBotOptions.ProxyType.HTTP;
            }
            botOptions.setProxyType(type);

        }

        Bot bot = new Bot(botOptions);
        for (int i = 0; i < 3; i++) {
            try {
                log.info("try start botSession... on " + config.getProxyType() + ":" + config.getProxyHost() + ":" + config.getProxyPort());
                botSession = botapi.registerBot(bot);
                log.info("botSession=" + botSession);
                break;
            } catch (TelegramApiException e) {
                log.error("Error start telegram bot", e);
            }
        }
        return bot;
    }

    private Bot(DefaultBotOptions botOptions) {
        super(botOptions);
    }

    @Override
    public String getBotUsername() {
        Config config = Setup.getSetup().getConfig(Config.class);
        return config.getUserName();
    }

    @Override
    public String getBotToken() {
        Config config = Setup.getSetup().getConfig(Config.class);
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update e) {
        // This will be what is executed when a private message is received.
        if (e.getMessage().isUserMessage() && e.hasMessage() && e.getMessage().hasText()) {
            Config config = Setup.getSetup().getConfig(Config.class);
            Message msg = e.getMessage();
            String text = msg.getText().toLowerCase();
            String chatId = Long.toString(msg.getChatId());
            if (log.isDebugEnabled()) {
                log.debug("Message: " + text);
            }

            if (text.equals("/start")) {
                sendMessage(chatId, config.getMsgDefaultAnswer());
                return;
            }
            if (text.equals("/login")) {
                userMap.put(chatId, new UserData());
                sendMessage(chatId, config.getMsgAskLogin());
                return;
            }
            if (text.equals("/getid")) {
                userMap.put(chatId, null);
                sendMessage(chatId, "Your telegramId=" + chatId);
                return;
            }
            if (text.equals("/help")) {
                userMap.put(chatId, null);
                sendMessage(chatId, config.getMsgUrlHelp());
                return;
            }

            UserData userData = userMap.get(chatId);
            if (userData != null) {
                if (userData.login == null) {
                    // waiting for input login
                    userData.login = text;
                    sendMessage(chatId, config.getMsgAskPassword());
                } else {
                    User user = UserCache.getUser(userData.login);
                    if (user == null || !user.getPassword().equals(text)) {
                        sendMessage(chatId, config.getMsgWrongPassword());
                        userMap.put(chatId, new UserData());
                        sendMessage(chatId, config.getMsgAskLogin());
                        return;
                    }
                    // Save in user param
                    Connection con = Setup.getSetup().getDBConnectionFromPool();
                    try {
                        ParamValueDAO paramDAO = new ParamValueDAO(con);
                        paramDAO.updateParamText(user.getId(), config.getParamId(), chatId);
                        con.commit();
                        userMap.put(chatId, null);
                        sendMessage(chatId, config.getMsgLinkChange());
                    } catch (Exception ex) {
                        log.error("Error storing subscription in Telegram ", ex);
                    } finally {
                        SQLUtils.closeConnection(con);
                    }
                }
            }
        }
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message was not sent ", e);
        }
    }

    public void sendMessage(String chatId, String text, String parseMode) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(parseMode)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message was not sent ", e);
        }
    }

}
