package ru.bgerp.plugin.telegram;

import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {

    private static final String DEFAULT_TOKEN = "";//842416376:AAFGsYLCgqG6eKcsZPy6gLCjge_klgUiWHE";
    private static final String DEFAULT_USER_NAME = "";//TestBgErp_bot";
    private final String token;
    private final String userName;
    private final int paramId; 
    private final int processParamId;
    private final boolean botStart;
    
    // соообщения бота, вдруг кому надо
    private final String msgWronPassword;
    private final String msgAskLogin;
    private final String msgAskPassword;
    private final String msgLinkChange;
    private final String msgStandartAnswer;
    private final String proxyHost;
    private final String proxyPort;
    private final String proxyType;
    

    public String getMsgStandartAnswer() {
        return msgStandartAnswer;
    }

    public Config(ParameterMap setup) {
        super(setup);

        setup = setup.sub(Plugin.ID + ":");

        botStart = setup.getBoolean("botStart,", setup.getBoolean("bot_start", false));

        token = setup.get("token", DEFAULT_TOKEN);
        userName = setup.get("botName", setup.get("bot_name", DEFAULT_USER_NAME));
        paramId = setup.getInt("userParamId", setup.getInt("param_id", -1));
        processParamId = setup.getInt("processParamId", setup.getInt("process_param_id", -1));

        proxyHost = setup.get("proxyHost", setup.get("proxy_host", null));
        proxyPort = setup.get("proxyPort", setup.get("proxy_port", null));
        proxyType = setup.get("proxyType", setup.get("proxy_type", null));
        
        //надо ли это людям, пока вопрос
        msgWronPassword = setup.get("msg_wrong_password", "Пользователь или пароль неверны");
        msgAskLogin = setup.get("msg_ask_login", "Для подключение к уведомлениям bgerp введите свой логин");
        msgAskPassword = setup.get("msg_ask_password", "Введите пароль");
        msgLinkChange = setup.get("msg_link_change", "Теперь вы будете получать уведомления");
        msgStandartAnswer = setup.get("msg_standart_answer", "Введите /start для подписки, или /getid,чтобы получить свой telegramId ");
        
        
    }

    public String getProxyType() {
        return proxyType;
    }

    public int getProcessParamId() {
        return processParamId;
    }

    public boolean isBotStart() {
        return botStart;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getMsgAskLogin() {
        return msgAskLogin;
    }

    public String getMsgAskPassword() {
        return msgAskPassword;
    }

    public String getToken() {
        return token;
    }

    public String getUserName() {
        return userName;
    }

    public String getMsgWronPassword() {
        return msgWronPassword;
    }
    public int getParamId() {
        return paramId;
    }

    public String getMsgLinkChange() {
        return msgLinkChange;
    }

}