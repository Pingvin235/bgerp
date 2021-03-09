package org.bgerp.plugin.telegram;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.ParameterMap;

public class Config extends ru.bgcrm.util.Config {

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
    private final String proxyHost;
    private final String proxyPort;
    private final String proxyType;

    protected Config(ParameterMap setup, boolean validate) throws BGMessageException {
        super(setup, validate);

        setup = setup.sub(Plugin.ID + ":");

        botStart = setup.getSokBoolean(false, validate, "botStart", "bot_start");

        token = setup.get("token", "");
        userName = setup.get("botName", "");
        paramId = setup.getInt("userParamId", -1);
        processParamId = setup.getInt("processParamId", -1);

        proxyHost = setup.get("proxyHost");
        proxyPort = setup.get("proxyPort");
        proxyType = setup.get("proxyType");

        msgWrongPassword = setup.get("msgWrongPassword", "Пользователь или пароль неверны");
        msgAskLogin = setup.get("msgAskLogin", "Для подключение к уведомлениям bgerp введите свой логин");
        msgAskPassword = setup.get("msgAskPassword", "Введите пароль");
        msgLinkChange = setup.get("msgLinkChange", "Теперь вы будете получать уведомления");
        msgDefaultAnswer = setup.get("msgDefaultAnswer", "Введите /start для подписки, или /getid,чтобы получить свой telegramId ");
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

}