package ru.bgcrm.struts.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Empty action, does nothing except passing to a JSP in parameter.
 * 
 * @author Shamil Vakhitov
 */
public class EmptyAction extends BaseAction {
    public EmptyAction() {
        super();
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) {
        var request = form.getHttpRequest();
        log.debug("r: %s; f: %s; q: %s", request, form, request.getQueryString());
        return html(conSet, form, form.getForwardFile());
    }
}
