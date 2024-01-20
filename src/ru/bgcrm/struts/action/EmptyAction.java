package ru.bgcrm.struts.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Empty action, does nothing except passing to a JSP in parameter.
 *
 * @author Shamil Vakhitov
 */
@Action(path = "/user/empty")
public class EmptyAction extends BaseAction {
    public EmptyAction() {
        super();
    }

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        var request = form.getHttpRequest();
        log.debug("r: {}; f: {}; q: {}", request, form, request.getQueryString());
        return html(conSet, form, form.getForwardFile());
    }
}
