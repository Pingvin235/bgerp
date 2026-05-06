package org.bgerp.plugin.msg.max;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGMessageException;

public class Config extends org.bgerp.app.cfg.Config {

    private final String token;
    private final String baseUrl;
    private final boolean botStart;
    private final int userParamId;
    private final int processParamId;

    protected Config(ConfigMap config, boolean validate) throws BGMessageException {
        super(null, validate);

        config = config.sub(Plugin.ID + ":");

        botStart = config.getBoolean("botStart", false);
        token = config.get("token", "");
        baseUrl = config.get("baseUrl", "https://platform-api.max.ru");
        userParamId = config.getInt("userParamId", -1);
        processParamId = config.getInt("processParamId", -1);
    }

    public String getToken() {
        return token;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isBotStart() {
        return botStart;
    }

    public int getUserParamId() {
        return userParamId;
    }

    public int getProcessParamId() {
        return processParamId;
    }

    /**
     * @return full API base URL, e.g. https://platform-api.max.ru
     */
    public String getApiUrl() {
        String url = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return url;
    }
}
