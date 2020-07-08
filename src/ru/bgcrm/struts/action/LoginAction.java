package ru.bgcrm.struts.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.event.client.UrlOpenEvent;
import ru.bgcrm.event.listener.LoginEventListener;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgerp.l10n.Localization;

public class LoginAction extends BaseAction {
    
    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        //TODO: Когда будет сделано событие авторизации, сделать его слушателя в LoginEventListener.
        User user = form.getUser();
        if (user != null) {
            String onLoginOpen = user.getConfigMap().getSok("on.login.open", "onLoginOpen");
            for (String url : Utils.toList(onLoginOpen))
                LoginEventListener.addOnLoginEvent(form.getUserId(), new UrlOpenEvent(url));
        }
        
        form.getHttpRequest().setAttribute("l", Localization.getLocalizer());

        // вывод страницы авторизации если responseType=html
        return data(conSet, mapping, form, FORWARD_DEFAULT);
    }
    
    public ActionForward logout(ActionMapping mapping, DynActionForm form, ConnectionSet conSet)
            throws Exception {
        form.getHttpRequest().getSession(true).invalidate();
        return status(conSet, form);
    }
    
}