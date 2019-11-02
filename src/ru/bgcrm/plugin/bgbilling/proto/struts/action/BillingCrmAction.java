package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.bgbilling.proto.dao.CrmDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.call.Call;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.task.Task;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Обращения к CRM плагину биллинга.
 */
public class BillingCrmAction
	extends BaseAction
{
	public ActionForward callList( ActionMapping mapping,
								   DynActionForm form,
								   HttpServletRequest request,
								   HttpServletResponse response,
								   ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		Integer contractId = form.getParamInt( "contractId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		SearchResult<Call> result = new SearchResult<Call>( form );
		crmDAO.getCallList( result, contractId );

		return processUserTypedForward( conSet, mapping, form, response, "callList" );
	}

	public ActionForward registerGroupList( ActionMapping mapping,
											DynActionForm form,
											HttpServletRequest request,
											HttpServletResponse response,
											ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "registerGroupList", crmDAO.getRegisterGroupList() );

		return processUserTypedForward( conSet, mapping, form, response, "registerGroupList" );
	}

	public ActionForward registerExecutorList( ActionMapping mapping,
											   DynActionForm form,
											   HttpServletRequest request,
											   HttpServletResponse response,
											   ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		String groupId = form.getParam( "groupId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "registerExecutorList", crmDAO.getRegisterExecutorList( groupId ) );

		return processUserTypedForward( conSet, mapping, form, response, "registerExecutorList" );
	}

	public ActionForward taskTypeList( ActionMapping mapping,
									   DynActionForm form,
									   HttpServletRequest request,
									   HttpServletResponse response,
									   ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "taskTypeList", crmDAO.getTaskTypeList() );

		return processUserTypedForward( conSet, mapping, form, response, "taskTypeList" );
	}

	public ActionForward callTypeList( ActionMapping mapping,
									   DynActionForm form,
									   HttpServletRequest request,
									   HttpServletResponse response,
									   ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "callTypeList", crmDAO.getCallRegisterTypeList() );

		return processUserTypedForward( conSet, mapping, form, response, "callTypeList" );
	}

	public ActionForward callCurrentProblemList( ActionMapping mapping,
												 DynActionForm form,
												 HttpServletRequest request,
												 HttpServletResponse response,
												 ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		Integer contractId = form.getParamInt( "contractId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "callCurrentProblemList", crmDAO.getCallCurrentProblemList( contractId ) );

		return processUserTypedForward( conSet, mapping, form, response, "callCurrentProblemList" );
	}

	public ActionForward createRegisterCall( ActionMapping mapping,
											 DynActionForm form,
											 HttpServletRequest request,
											 HttpServletResponse response,
											 ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		Integer contractId = form.getParamInt( "contractId" );
		Integer typeId = form.getParamInt( "typeId" );
		Integer problemGroupId = form.getParamInt( "registerGroupId" );
		String comment = form.getParam( "comment" );
		String problemComment = form.getParam( "problemComment" );
		Integer linkProblemId = form.getParamInt( "linkProblemId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );
		crmDAO.updateRegisterCall( contractId, typeId, problemGroupId, linkProblemId, comment, problemComment );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward getRegisterSubjectGroup( ActionMapping mapping,
												  DynActionForm form,
												  HttpServletRequest request,
												  HttpServletResponse response,
												  ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		Integer typeId = form.getParamInt( "typeId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "groupId", crmDAO.getRegisterSubjectGroup( typeId ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward taskList( ActionMapping mapping,
								   DynActionForm form,
								   HttpServletRequest request,
								   HttpServletResponse response,
								   ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		Integer contractId = form.getParamInt( "contractId" );

		String sort1 = form.getParam( "sort1" );
		String sort2 = form.getParam( "sort2" );

		SearchResult<Task> result = new SearchResult<Task>( form );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );
		crmDAO.getTaskList( result, contractId, sort1, sort2 );

		return processUserTypedForward( conSet, mapping, form, response, "taskList" );
	}

	public ActionForward taskGet( ActionMapping mapping,
								  DynActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response,
								  ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		Integer taskId = form.getParamInt( "taskId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "task", crmDAO.getTask( taskId ) );

		return processUserTypedForward( conSet, mapping, form, response, "taskEditor" );
	}

	public ActionForward createRegisterTask( ActionMapping mapping,
											 DynActionForm form,
											 HttpServletRequest request,
											 HttpServletResponse response,
											 ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int addressParameterId = form.getParamInt( "contractAddressId" );
		int typeId = form.getParamInt( "taskTypeId" );
		int groupId = form.getParamInt( "registerGroupId" );
		int statusId = form.getParamInt( "statusId" );
		String targetDate = form.getParam( "targetDate" );
		String executeDate = form.getParam( "executeDate" );
		String comment = form.getParam( "taskComment" );
		String resolution = form.getParam( "taskResolution" );
		List<Integer> executorList = form.getSelectedValuesList( "executor" );
		String executors = executorList.toString().replaceAll( "\\]|\\[", "" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );
		crmDAO.createTask( contractId, 0, typeId, groupId, statusId, targetDate, "", executeDate, executors, resolution, addressParameterId, comment );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward updateRegisterTask( ActionMapping mapping,
											 DynActionForm form,
											 HttpServletRequest request,
											 HttpServletResponse response,
											 ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int addressParameterId = form.getParamInt( "contractAddressId" );
		int typeId = form.getParamInt( "taskTypeId" );
		int taskId = form.getParamInt( "taskId" );
		int groupId = form.getParamInt( "registerGroupId" );
		int statusId = form.getParamInt( "statusId" );
		String targetDate = form.getParam( "targetDate" );
		String executeDate = form.getParam( "executeDate" );
		String comment = form.getParam( "taskComment" );
		String resolution = form.getParam( "taskResolution" );
		List<Integer> executorList = form.getSelectedValuesList( "executor" );
		String executors = executorList.toString().replaceAll( "\\]|\\[", "" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );
		crmDAO.updateTask( taskId, contractId, 0, typeId, groupId, statusId, targetDate, "", executeDate, executors, resolution, addressParameterId, comment );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward updateRegisterProblem( ActionMapping mapping,
												DynActionForm form,
												HttpServletRequest request,
												HttpServletResponse response,
												ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		Integer problemId = form.getParamInt( "problemId" );
		Integer groupId = form.getParamInt( "registerGroupId" );
		Integer urgency = form.getParamInt( "urgency" );
		Integer statusId = form.getParamInt( "statusId" );
		String comment = form.getParam( "problemComment" );
		List<Integer> executorList = form.getSelectedValuesList( "executor" );
		String executors = executorList.toString().replaceAll( "\\]|\\[", "" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );
		form.getResponse().setData( "problem", crmDAO.updateRegisterProblem( problemId, statusId, groupId, executors, comment, urgency ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward getRegisterProblem( ActionMapping mapping,
											 DynActionForm form,
											 HttpServletRequest request,
											 HttpServletResponse response,
											 ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		Integer problemId = form.getParamInt( "problemId" );

		if( problemId > 0 )
		{
			CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );
			form.getResponse().setData( "problem", crmDAO.getRegisterProblem( problemId ) );
		}
		return processUserTypedForward( conSet, mapping, form, response, "getRegisterProblem" );
	}

	public ActionForward registerProblemListItem( ActionMapping mapping,
												  DynActionForm form,
												  HttpServletRequest request,
												  HttpServletResponse response,
												  ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		Integer problemId = form.getParamInt( "problemId" );

		CrmDAO crmDAO = new CrmDAO( form.getUser(), billingId );

		form.getResponse().setData( "problem", crmDAO.getRegisterProblem( problemId ) );

		return processUserTypedForward( conSet, mapping, form, response, "registerProblemListItem" );
	}
}
