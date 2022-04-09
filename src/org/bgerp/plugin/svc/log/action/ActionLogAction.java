package org.bgerp.plugin.svc.log.action;

import java.time.YearMonth;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;
import org.bgerp.plugin.svc.log.Plugin;
import org.bgerp.plugin.svc.log.dao.ActionLogDAO;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/log/action")
public class ActionLogAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setRequestAttribute("permTrees", UserCache.getPermTrees());

        return html(conSet, form, PATH_JSP + "/action.jsp");
    }

    public ActionForward search(DynActionForm form, ConnectionSet conSet) throws Exception {
        Date timeFrom = form.getParamDateTime("timeFrom");
        if (timeFrom == null)
            throw new BGMessageException("Time from can't be empty.");

        Date timeTo = form.getParamDateTime("timeTo");
        if (timeTo != null && !YearMonth.from(timeFrom.toInstant()).equals(YearMonth.from(timeTo.toInstant())))
            throw new BGMessageException("Time to must be in the same month as time from.");

        new ActionLogDAO(conSet.getSlaveConnection())
            .withTimeFrom(timeFrom)
            .withTimeTo(timeTo)
            .withIpAddress(form.getParam("ipAddress"))
            .withParameter(form.getParam("parameter"))
            .withUserIds(form.getSelectedValues("userId"))
            .withActions(form.getSelectedValuesStr("perm"))
            .search(new Pageable<>(form));

        return unspecified(form, conSet);
    }
}
