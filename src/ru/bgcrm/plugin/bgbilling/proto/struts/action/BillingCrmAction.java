package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.Plugin;
import ru.bgcrm.plugin.bgbilling.proto.dao.CrmDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.call.Call;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.task.Task;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Обращения к CRM плагину биллинга.
 */
@Action(path = "/user/plugin/bgbilling/proto/billingCrm")
public class BillingCrmAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/crm";

    public ActionForward callList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        Pageable<Call> result = new Pageable<Call>(form);
        crmDAO.getCallList(result, contractId);

        return html(conSet, form, PATH_JSP + "/call/call_list.jsp");
    }

    public ActionForward registerGroupList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("registerGroupList", crmDAO.getRegisterGroupList());

        return html(conSet, form, PATH_JSP + "/register_group_list.jsp");
    }

    public ActionForward registerExecutorList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        String groupId = form.getParam("groupId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("registerExecutorList", crmDAO.getRegisterExecutorList(groupId));

        return html(conSet, form, PATH_JSP + "/register_executor_list.jsp");
    }

    public ActionForward taskTypeList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("taskTypeList", crmDAO.getTaskTypeList());

        return html(conSet, form, PATH_JSP + "/task/task_type_list.jsp");
    }

    public ActionForward callTypeList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("callTypeList", crmDAO.getCallRegisterTypeList());

        return html(conSet, form, PATH_JSP + "/call/call_type_list.jsp");
    }

    public ActionForward callCurrentProblemList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("callCurrentProblemList", crmDAO.getCallCurrentProblemList(contractId));

        return html(conSet, form, PATH_JSP + "/call/call_current_problem_list.jsp");
    }

    public ActionForward createRegisterCall(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");
        Integer typeId = form.getParamInt("typeId");
        Integer problemGroupId = form.getParamInt("registerGroupId");
        String comment = form.getParam("comment");
        String problemComment = form.getParam("problemComment");
        Integer linkProblemId = form.getParamInt("linkProblemId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
        crmDAO.updateRegisterCall(contractId, typeId, problemGroupId, linkProblemId, comment, problemComment);

        return json(conSet, form);
    }

    public ActionForward getRegisterSubjectGroup(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer typeId = form.getParamInt("typeId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("groupId", crmDAO.getRegisterSubjectGroup(typeId));

        return json(conSet, form);
    }

    public ActionForward taskList(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer contractId = form.getParamInt("contractId");

        String sort1 = form.getParam("sort1");
        String sort2 = form.getParam("sort2");

        Pageable<Task> result = new Pageable<Task>(form);

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
        crmDAO.getTaskList(result, contractId, sort1, sort2);

        return html(conSet, form, PATH_JSP + "/task/task_list.jsp");
    }

    public ActionForward taskGet(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer taskId = form.getParamInt("taskId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("task", crmDAO.getTask(taskId));

        return html(conSet, form, PATH_JSP + "/task/task_editor.jsp");
    }

    public ActionForward createRegisterTask(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int addressParameterId = form.getParamInt("contractAddressId");
        int typeId = form.getParamInt("taskTypeId");
        int groupId = form.getParamInt("registerGroupId");
        int statusId = form.getParamInt("statusId");
        String targetDate = form.getParam("targetDate");
        String executeDate = form.getParam("executeDate");
        String comment = form.getParam("taskComment");
        String resolution = form.getParam("taskResolution");
        List<Integer> executorList = form.getSelectedValuesList("executor");
        String executors = executorList.toString().replaceAll("\\]|\\[", "");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
        crmDAO.createTask(contractId, 0, typeId, groupId, statusId, targetDate, "", executeDate, executors, resolution,
                addressParameterId, comment);

        return json(conSet, form);
    }

    public ActionForward updateRegisterTask(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        int contractId = form.getParamInt("contractId");
        int addressParameterId = form.getParamInt("contractAddressId");
        int typeId = form.getParamInt("taskTypeId");
        int taskId = form.getParamInt("taskId");
        int groupId = form.getParamInt("registerGroupId");
        int statusId = form.getParamInt("statusId");
        String targetDate = form.getParam("targetDate");
        String executeDate = form.getParam("executeDate");
        String comment = form.getParam("taskComment");
        String resolution = form.getParam("taskResolution");
        List<Integer> executorList = form.getSelectedValuesList("executor");
        String executors = executorList.toString().replaceAll("\\]|\\[", "");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
        crmDAO.updateTask(taskId, contractId, 0, typeId, groupId, statusId, targetDate, "", executeDate, executors,
                resolution, addressParameterId, comment);

        return json(conSet, form);
    }

    public ActionForward updateRegisterProblem(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        Integer problemId = form.getParamInt("problemId");
        Integer groupId = form.getParamInt("registerGroupId");
        Integer urgency = form.getParamInt("urgency");
        Integer statusId = form.getParamInt("statusId");
        String comment = form.getParam("problemComment");
        List<Integer> executorList = form.getSelectedValuesList("executor");
        String executors = executorList.toString().replaceAll("\\]|\\[", "");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
        form.getResponse().setData("problem",
                crmDAO.updateRegisterProblem(problemId, statusId, groupId, executors, comment, urgency));

        return json(conSet, form);
    }

    public ActionForward getRegisterProblem(DynActionForm form, ConnectionSet conSet) throws Exception {
        String billingId = form.getParam("billingId");
        Integer problemId = form.getParamInt("problemId");

        if (problemId > 0) {
            CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);
            form.getResponse().setData("problem", crmDAO.getRegisterProblem(problemId));
        }
        return html(conSet, form, PATH_JSP + "/problem/problem_editor.jsp");
    }

    public ActionForward registerProblemListItem(DynActionForm form, ConnectionSet conSet) throws BGException {
        String billingId = form.getParam("billingId");
        Integer problemId = form.getParamInt("problemId");

        CrmDAO crmDAO = new CrmDAO(form.getUser(), billingId);

        form.getResponse().setData("problem", crmDAO.getRegisterProblem(problemId));

        return html(conSet, form, PATH_JSP + "/problem/problem_list_item.jsp");
    }
}
