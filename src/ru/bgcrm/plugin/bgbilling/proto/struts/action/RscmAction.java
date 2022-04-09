package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.proto.dao.DirectoryDAO;
import ru.bgcrm.plugin.bgbilling.proto.dao.RscmDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.rscm.RscmService;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.ConnectionSet;

public class RscmAction
    extends BaseAction
{
	public ActionForward serviceList( ActionMapping mapping, DynActionForm form,
	                                  HttpServletRequest request, HttpServletResponse response,
	                                  ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );
		Calendar curdate = new GregorianCalendar();
		Date dateFrom = form.getParamDate( "dateFrom", TimeUtils.getStartMonth( curdate ).getTime());
		Date dateTo = form.getParamDate( "dateTo", TimeUtils.getEndMonth( curdate ).getTime() );

		new RscmDAO( form.getUser(), billingId, moduleId )
			.getServices( new Pageable<RscmService>( form ), contractId, dateFrom, dateTo );;

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
			form.getResponse().setData( "service", new RscmDAO( form.getUser(), billingId, moduleId ).getService( contractId, form.getId() ) );
		}
		form.getResponse().setData( "serviceTypeList", new DirectoryDAO( form.getUser(), billingId ).getServiceTypeList( moduleId ) );

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

		RscmService service = new RscmService();
		service.setId( form.getId() );
		service.setContractId( contractId );
		service.setServiceId( form.getParamInt( "serviceId" ) );
		service.setDate( form.getParamDate( "date" ) );
		service.setAmount( form.getParamInt( "amount" ) );
		service.setComment( form.getParam( "comment", "" ) );

		new RscmDAO( form.getUser(), billingId, moduleId ).updateService( service );

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
		Date month = form.getParamDate( "month" );

		new RscmDAO( form.getUser(), billingId, moduleId ).deleteService( contractId, form.getId(), month  );

		return processJsonForward( conSet, form, response );
	}
}