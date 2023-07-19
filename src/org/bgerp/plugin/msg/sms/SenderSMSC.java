package org.bgerp.plugin.msg.sms;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

public class SenderSMSC extends Sender {
    private static final Log log = Log.getLog();

    private final String url;
    private final String login;
    private final String password;

    protected SenderSMSC(ConfigMap setup) throws InitStopException {
        super(setup);
        url = setup.get("url", "https://smsc.ru/sys/send.php");
        login = setup.get("login");
        password = setup.get("password");
        initWhen(StringUtils.isNoneBlank(login, password));
    }

    @Override
    public void send(String number, String text) {
        try {
            URIBuilder url = new URIBuilder(this.url);
            url.addParameter("login", login);
            url.addParameter("psw", password);
            url.addParameter("phones", number);
            url.addParameter("mes", text);

            Request req = Request.Get(url.build());
            log.debug("Sending: {}", req);

            String response = req.execute().returnContent().asString(StandardCharsets.UTF_8);
            log.debug("=> {}", response);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
