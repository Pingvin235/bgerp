package ru.bgcrm.plugin.task.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;

import ru.bgcrm.plugin.task.Config;
import ru.bgcrm.plugin.task.Plugin;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/task/process", pathId = true)
public class TaskAction extends org.bgerp.action.base.BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setRequestAttribute("config", setup.getConfig(Config.class));
        new TaskDAO(conSet.getSlaveConnection()).searchTasks(new Pageable<>(form), form.getId(), 0, true);
        return html(conSet, form, PATH_JSP + "/process.jsp");
    }
}
