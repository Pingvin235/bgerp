package org.bgerp.plugin.telegram;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.dist.App;
import org.bgerp.app.exception.BGMessageException;

public class Config extends org.bgerp.app.cfg.Config {

    private final String token;
    private final String userName;
    private final int paramId;
    private final int processParamId;
    private final boolean botStart;

    private final String msgWrongPassword;
    private final String msgAskLogin;
    private final String msgAskPassword;
    private final String msgLinkChange;
    private final String msgDefaultAnswer;
    private final String msgUrlHelp;
    private final String proxyHost;
    private final String proxyPort;
    private final String proxyType;

    protected Config(ConfigMap config, boolean validate) throws BGMessageException {
        super(null, validate);

        config = config.sub(Plugin.ID + ":");

        botStart = config.getSokBoolean(false, validate, "botStart", "bot_start");

        token = config.get("token", "");
        userName = config.getSok("botName", validate,"botName", "bot_name");
        paramId = config.getInt("userParamId", -1);
        processParamId = config.getInt("processParamId", -1);

        proxyHost = config.get("proxyHost");
        proxyPort = config.get("proxyPort");
        proxyType = config.get("proxyType");

        msgWrongPassword = config.get("msgWrongPassword", "Пользователь или пароль неверны");
        msgAskLogin = config.get("msgAskLogin", "Для подключение к уведомлениям bgerp введите свой логин");
        msgAskPassword = config.get("msgAskPassword", "Введите пароль");
        msgLinkChange = config.get("msgLinkChange", "Теперь вы будете получать уведомления");
        msgDefaultAnswer = config.get("msgDefaultAnswer", "Введите /login для подписки или /getid, чтобы получить свой telegramId");
        msgUrlHelp = config.get("msgUrlHelp", App.URL + "/doc/3.0/manual/plugin/telegram/index.html");
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

    public String getMsgWrongPassword() {
        return msgWrongPassword;
    }

    public int getParamId() {
        return paramId;
    }

    public String getMsgLinkChange() {
        return msgLinkChange;
    }

    public String getMsgDefaultAnswer() {
        return msgDefaultAnswer;
    }

    public String getMsgUrlHelp() {
        return msgUrlHelp;
    }

}