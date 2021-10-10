package ru.bgcrm.plugin.task.action;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;

import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.task.Plugin;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;

@Action(path = "/user/plugin/task/task")
public class TaskAction extends ru.bgcrm.struts.action.BaseAction {
    private static final String JSP_PATH = PATH_JSP_USER_PLUGIN + "/" + Plugin.ID;

    public ActionForward list(DynActionForm form, Connection con) throws Exception {
        new TaskDAO(con).searchTasks(new SearchResult<>(form), form.getParamInt("processId"), 0, true);
        return html(con, form, JSP_PATH + "/task_list.jsp");
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
