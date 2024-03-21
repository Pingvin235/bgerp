package org.bgerp.plugin.svc.log.action.admin;

import java.time.YearMonth;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.model.Pageable;
import org.bgerp.plugin.svc.log.Plugin;
import org.bgerp.plugin.svc.log.dao.ActionLogDAO;

import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/log/action")
public class ActionLogAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setRequestAttribute("permTrees", PermissionNode.getPermissionTrees());

        return html(conSet, form, PATH_JSP + "/action.jsp");
    }

    public ActionForward search(DynActionForm form, ConnectionSet conSet) throws Exception {
        Date timeFrom = form.getParamDateTime("timeFrom");
        if (timeFrom == null)
            throw new BGMessageException("Time from can't be empty.");

        Date timeTo = form.getParamDateTime("timeTo");
        if (timeTo != null && !YearMonth.from(timeFrom.toInstant()).equals(YearMonth.from(timeTo.toInstant())))
            throw new BGMessageException("Time to must be in the same month as time from.");

        Set<String> actions = form.getParamValuesStr("perm");
        Set<String> allActions = new TreeSet<>(actions);

        for (String action : actions)
            allActions.addAll(PermissionNode.getPermissionNodeOrThrow(action).getActions());

        new ActionLogDAO(conSet.getSlaveConnection())
            .withTimeFrom(timeFrom)
            .withTimeTo(timeTo)
            .withIpAddress(form.getParam("ipAddress"))
            .withParameter(form.getParam("parameter"))
            .withUserIds(form.getParamValues("userId"))
            .withActions(allActions)
            .search(new Pageable<>(form));

        return unspecified(form, conSet);
    }
}
