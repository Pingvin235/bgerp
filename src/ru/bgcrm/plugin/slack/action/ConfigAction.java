package ru.bgcrm.plugin.slack.action;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.struts.action.ActionForward;

import ru.bgcrm.plugin.slack.Plugin;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/slack/config")
public class ConfigAction extends org.bgerp.action.base.BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return html(conSet, form, PATH_JSP + "/config.jsp");
    }

    public ActionForward token(DynActionForm form, ConnectionSet conSet) throws Exception {
        final var clientId = form.getParam("clientId", Utils::notBlankString);
        final var authCode = form.getParam("authCode", Utils::notBlankString);
        final var clientSecret = form.getParam("clientSecret", Utils::notBlankString);

        final var log = new StringWriter(1000);
        final var logWriter = new PrintWriter(log);

        // https://api.slack.com/authentication/oauth-v2#exchanging
        try {
            var url = new URIBuilder("https://slack.com/api/oauth.v2.access");
            url.addParameter("client_id", clientId);
            url.addParameter("client_secret", clientSecret);
            url.addParameter("code", authCode);
            url.addParameter("redirect_uri", "https://localhost");
            var req = Request.Post(url.build());

            logWriter.println("REQUEST:");
            logWriter.println(req);
            logWriter.println();
            logWriter.println("RESPONSE:");
            logWriter.println(req.execute().returnContent().asString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace(logWriter);
        }

        form.setResponseData("log", log.toString());

        return unspecified(form, conSet);
    }
}