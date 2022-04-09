package ru.bgcrm.plugin.task.action;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;

import ru.bgcrm.plugin.task.Plugin;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;

@Action(path = "/user/plugin/task/task")
public class TaskAction extends ru.bgcrm.struts.action.BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward list(DynActionForm form, Connection con) throws Exception {
        new TaskDAO(con).searchTasks(new Pageable<>(form), form.getParamInt("processId"), 0, true);
        return html(con, form, PATH_JSP + "/task_list.jsp");
    }

    public ActionForward update(DynActionForm form, Connection con) throws Exception {
        // TODO: Реализовать.
        return json(con, form);
    }

    public ActionForward delete(DynActionForm form, Connection con) throws Exception {
        // TODO: Реализовать.
        return json(con, form);
    }
}
