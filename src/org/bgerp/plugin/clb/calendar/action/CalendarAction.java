package org.bgerp.plugin.clb.calendar.action;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.plugin.clb.calendar.Config;
import org.bgerp.plugin.clb.calendar.Plugin;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/calendar/calendar")
public class CalendarAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        var config = setup.getConfig(Config.class);

        form.setRequestAttribute("config", config);

        Date week = form.getParamDate("week", TimeConvert.toDate(LocalDate.now().with(DayOfWeek.MONDAY)));
        form.setResponseData("weekPrev", TimeConvert.toDate(TimeConvert.toLocalDate(week).minusWeeks(1)));
        form.setResponseData("weekNext", TimeConvert.toDate(TimeConvert.toLocalDate(week).plusWeeks(1)));

        if (form.getId() > 0) {
            var calendar = config.getCalendarMap().get(form.getId());
        }

        return html(conSet, form, PATH_JSP + "/calendar.jsp");
    }
}
