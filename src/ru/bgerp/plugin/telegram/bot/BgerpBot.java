package ru.bgerp.plugin.telegram.bot;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.user.User;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgerp.plugin.telegram.Config;

public class BgerpBot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        init();
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(TelegramLongPollingBot.class);

    private static BgerpBot instance;

    private Map<Long, UserData> userMap = new HashMap<>();

    private static BotSession botSession = null;

    private BgerpBot() {
    };

    public static BgerpBot getInstance() {

        if (instance == null) {
            reinit();
            /*if (instance != null) {
                EventProcessor.subscribe(new EventListener<SetupChangedEvent>() {
                    @Override
                    public void notify(SetupChangedEvent e, ConnectionSet connectionSet) {
                        reinit();
                    }
                }, SetupChangedEvent.class);
            }*/
        }
        return instance;
    }

    private static void reinit() {
        log.info( "Reinit..telegramBot" );
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

    private static BgerpBot init() {

        // пробуем остановить старую, на всяий случай
        try {
            if (botSession != null) {
                botSession.stop();
                botSession = null;
            }
        } catch (Exception e) {
            log.error("Catch exception", e);
        }
        ApiContextInitializer.init(); // Инициализируем апи

        TelegramBotsApi botapi = new TelegramBotsApi();
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
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
        BgerpBot bot = new BgerpBot(botOptions);
        for (int i = 0; i < 3; i++) {

            try {
                log.info("try start botSession...on " + config.getProxyType() + ":" + config.getProxyHost() + ":" + config.getProxyPort());
                botSession = botapi.registerBot(bot);
                log.info("botSession=" + botSession);
                break;
            } catch (TelegramApiException e) {
                log.error("Error start telegramm bot", e);
            }
        }
        return bot;
    }

    private BgerpBot(DefaultBotOptions botOptions) {
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
        // Тут будет то, что выполняется при получении сообщения
        Config config = Setup.getSetup().getConfig(Config.class);
        Message msg = e.getMessage();
        String txt = msg.getText();
        Long chatId = msg.getChatId();
        if (txt.equals("/start")) {
            userMap.put(chatId, new UserData());
            sendMessage(chatId, config.getMsgAskLogin());
            return;
        }
        if (txt.equals("/getid")) {
            userMap.put(chatId, null);
            sendMessage(chatId, "Ваш telegramId=" + chatId);
            return;
        }
        if (txt.equals("/cancel")) {
            userMap.put(chatId, null);
            sendMessage(chatId, config.getMsgStandartAnswer());
            return;
        }
        UserData userData = userMap.get(chatId);
        if (userData != null) {
            if (userData.login == null) {
                // ожидаем ввод логина
                userData.login = txt;
                sendMessage(chatId, config.getMsgAskPassword());
                return;
            } else {
                User user = UserCache.getUser(userData.login);
                if (user == null || !user.getPassword().equals(txt)) {

                    sendMessage(chatId, config.getMsgWronPassword());
                    userMap.put(chatId, new UserData());
                    sendMessage(chatId, config.getMsgAskLogin());
                    return;
                }
                // сохраняем в параметре
                Connection con = Setup.getSetup().getDBConnectionFromPool();
                try {
                    ParamValueDAO paramDAO = new ParamValueDAO(con);
                    paramDAO.updateParamText(user.getId(), config.getParamId(), String.valueOf(chatId));
                    con.commit();
                    userMap.put(chatId, null);
                    sendMessage(chatId, config.getMsgLinkChange());

                } catch (Exception ex) {
                    log.error("Ощибка сохранения подписки в telegram", ex);
                } finally {

                    SQLUtils.closeConnection(con);
                }
            }

        }
        // просто ответ.
        sendMessage(chatId, config.getMsgStandartAnswer());

    }

    public void sendMessage(Long chanId, String text) {
        SendMessage s = new SendMessage();
        s.setChatId(chanId);
        s.setText(text);
        /*
         * try { executeAsync(s, new SentCallback<Message>() {
         * 
         * @Override public void onResult(BotApiMethod<Message> method, Message
         * response) { System.out.println("Сообщение отправлено"); }
         * 
         * @Override public void onError(BotApiMethod<Message> method,
         * TelegramApiRequestException apiException) { System.out.println("ERROR:" +
         * apiException); }
         * 
         * @Override public void onException(BotApiMethod<Message> method, Exception
         * exception) { System.out.println("EXCEPTION:" + exception); init(); } }); }
         * catch (TelegramApiException e) { // TODO Auto-generated catch block //
         * e.printStackTrace();
         * 
         * }
         */
        try {
            execute(s);
        } catch (TelegramApiException e) {
            // TODO Auto-generated catch block
            log.error("Message not send", e);

        }
    }

    private class UserData {

        public String login = null;

    }
}
