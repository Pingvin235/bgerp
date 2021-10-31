package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.plugin.bgbilling.proto.dao.ContractObjectDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.NPayDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.npay.NPayService;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class NPayAction
	extends BaseAction
{
	public ActionForward serviceList( ActionMapping mapping,
									  DynActionForm form,
									  HttpServletRequest request,
									  HttpServletResponse response,
									  ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );
		
		form.getResponse().setData( "list", NPayDAO.getInstance( form.getUser(), billingId, moduleId ).getServiceList( contractId ) );
		
		return processUserTypedForward( conSet, mapping, form, response, "serviceList" );
	}

	public ActionForward serviceGet( ActionMapping mapping,
	                                 DynActionForm form,
	                                 HttpServletRequest request,
	                                 HttpServletResponse response,
	                                 ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		if( form.getId() > 0 )
		{
			form.getResponse().setData( "service", NPayDAO.getInstance( form.getUser(), billingId, moduleId ).getService( form.getId() ) );
		}
		form.getResponse().setData( "serviceTypeList", new DirectoryDAO( form.getUser(), billingId ).getServiceTypeList( moduleId ) );
		form.getResponse().setData( "objectList", new ContractObjectDAO( form.getUser(), billingId ).getContractObjects( contractId ) );

		return processUserTypedForward( conSet, mapping, form, response, "serviceEditor" );
	}

	public ActionForward serviceUpdate( ActionMapping mapping,
	                                    DynActionForm form,
	                                    HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		NPayService service = new NPayService();
		service.setId( form.getId() );
		service.setContractId( contractId );
		service.setServiceId( form.getParamInt( "serviceId" ) );
		service.setDateFrom( form.getParamDate( "dateFrom" ) );
		service.setDateTo( form.getParamDate( "dateTo" ) );
		service.setObjectId( form.getParamInt( "objectId" ) );
		service.setComment( form.getParam( "comment", "" ) );
		
		NPayDAO.getInstance( form.getUser(), billingId, moduleId ).updateService( service );
		
		return processJsonForward( conSet, form, response );
	}
	
	public ActionForward serviceDelete( ActionMapping mapping,
	                                    DynActionForm form,
	                                    HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    ConnectionSet conSet )
		throws Exception
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		NPayDAO.getInstance( form.getUser(), billingId, moduleId ).deleteService( contractId, form.getId() );
		
		return processJsonForward( conSet, form, response );
	}
}