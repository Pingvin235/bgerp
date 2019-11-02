package ru.bgcrm.plugin.task.struts.action;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.task.dao.TaskDAO;
import ru.bgcrm.struts.form.DynActionForm;

public class TaskAction extends ru.bgcrm.struts.action.BaseAction {

    public ActionForward list(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new TaskDAO(con).searchTasks(new SearchResult<>(form), form.getParamInt("processId"), 0, true);
        return processUserTypedForward(con, mapping, form, "list");
    }
    
    public ActionForward update(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        // TODO: Реализовать.
        return processJsonForward(con, form);
    }
    
    public ActionForward delete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        // TODO: Реализовать.
        return processJsonForward(con, form);
    }
}
