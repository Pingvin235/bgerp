package org.bgerp.plugin.calendar.action;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.calendar.Plugin;
import org.bgerp.plugin.calendar.dao.TimeBalanceDAO;
import org.bgerp.plugin.calendar.model.TimeBalance;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Action(path = "/user/plugin/calendar/balance")
public class ActionBalance extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward show(DynActionForm form, Connection con) throws Exception {
        var activeUsers = UserCache.getActiveUsers();

        var balances = new TimeBalanceDAO(con).getBalances(form.getParamInt("year", LocalDate.now().getYear()), Utils.getObjectIdsList(activeUsers));

        List<TimeBalance> list = new ArrayList<>(activeUsers.size());
        for (var user : activeUsers) {

        }

        return html(con, form, PATH_JSP + "/balance.jsp");
    }
}
