package org.bgerp.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.l10n.Localization;

import ru.bgcrm.event.client.UrlOpenEvent;
import ru.bgcrm.event.listener.LoginEventListener;
import ru.bgcrm.model.user.User;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.servlet.filter.SetRequestParamsFilter;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/login")
public class LoginAction extends BaseAction {

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        User user = form.getUser();
        if (user != null) {
            String onLoginOpen = user.getConfigMap().getSok("on.login.open", "onLoginOpen");
            for (String url : Utils.toList(onLoginOpen))
                LoginEventListener.addOnLoginEvent(form.getUserId(), new UrlOpenEvent(url));

            form.setResponseData("title", user.getTitle());
        }

        form.setRequestAttribute(SetRequestParamsFilter.REQUEST_KEY_LOCALIZER, Localization.getSysLocalizer());

        return html(conSet, user != null ? form : null, "/login.jsp");
    }

    public ActionForward logout(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.getHttpRequest().getSession(true).invalidate();
        return json(conSet, form);
    }

}