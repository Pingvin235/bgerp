package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.proto.dao.IpnDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ipn.IpnRange;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.struts.form.Response;
import ru.bgcrm.util.sql.ConnectionSet;

public class IpnAction
	extends BaseAction
{
	public ActionForward rangeList( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );
		Date date = form.getParamDate( "date" );

		IpnDAO ipnDao = new IpnDAO( form.getUser(), billingId, moduleId );
		form.getResponse().setData( "rangeList", ipnDao.getIpnRanges( contractId, date, false ) );
		form.getResponse().setData( "netList", ipnDao.getIpnRanges( contractId, date, true ) );

		return processUserTypedForward( conSet, mapping, form, response, "rangeList" );
	}

	public ActionForward rangeEdit( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		IpnDAO ipnDao = new IpnDAO( form.getUser(), billingId, moduleId );
		form.getResponse().setData( "range", ipnDao.getIpnRange( contractId, form.getId() ) );
		form.getResponse().setData( "sourceList", ipnDao.getSourceList( new Date() ) );
		form.getResponse().setData( "planList", ipnDao.linkPlanList() );

		return processUserTypedForward( conSet, mapping, form, response, "rangeEdit" );
	}

	public ActionForward rangeDelete( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		new IpnDAO( form.getUser(), billingId, moduleId ).deleteIpnRange( contractId, form.getId() );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward rangeUpdate( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		IpnDAO ipnDao = new IpnDAO( form.getUser(), billingId, moduleId );

		IpnRange range = new IpnRange();
		range.setId( form.getId() );
		range.setContractId( contractId );
		range.setAddressFrom( form.getParam( "addressFrom" ) );
		range.setAddressTo( form.getParam( "addressTo" ) );
		range.setMask( form.getParamInt( "mask" ) );
		range.setDateFrom( form.getParamDate( "dateFrom" ) );
		range.setDateTo( form.getParamDate( "dateTo" ) );
		range.setIfaceList( form.getSelectedValuesListStr( "iface" ) );
		range.setPlan( form.getParamInt( "plan" ) );
		range.setComment( form.getParam( "comment" ) );

		ipnDao.updateIpnRange( range );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward gateStatus( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		IpnDAO ipnDao = new IpnDAO( form.getUser(), billingId, moduleId );
		form.getResponse().setData( "info", ipnDao.gateInfo( contractId ) );

		return processUserTypedForward( conSet, mapping, form, response, "gateStatus" );
	}

	public ActionForward gateStatusUpdate( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		IpnDAO ipnDao = new IpnDAO( form.getUser(), billingId, moduleId );
		ipnDao.gateStatusUpdate( contractId, form.getParamInt( "status" ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward gateRuleEdit( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		int gateTypeId = form.getParamInt( "gateTypeId" );
		int gateId = form.getParamInt( "gateId" );
		// int userGateId = form.getParamInt( "userGateId" );

		IpnDAO ipnDao = new IpnDAO( form.getUser(), billingId, moduleId );

		Response resp = form.getResponse();
		if( gateId > 0 )
		{
			resp.setData( "ruleTypeList", ipnDao.gateRuleTypeList( gateTypeId ) );
			if( form.getId() > 0 )
			{
				resp.setData( "rulePair", ipnDao.getUserGateRule( form.getId() ) );
			}

			resp.setData( "rangeList", ipnDao.getIpnRanges( contractId, form.getParamDate( "date" ), false ) );
			resp.setData( "netList", ipnDao.getIpnRanges( contractId, form.getParamDate( "date" ), true ) );

			return processUserTypedForward( conSet, mapping, form, response, "gateEdit" );
		}
		else
		{
			resp.setData( "gateList", ipnDao.getGateList() );

			return processUserTypedForward( conSet, mapping, form, response, "gateSelect" );
		}
	}

	public ActionForward gateRuleGenerate( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		// int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );
		// Date date = form.getParamDate( "date" );

		IpnDAO ipnDao = new IpnDAO( form.getUser(), billingId, moduleId );

		String rule = ipnDao.generateRule( form.getParamInt( "ruleTypeId" ), form.getParamInt( "gateTypeId" ), form.getParam( "addressList" ) );
		form.getResponse().setData( "rule", rule );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward gateRuleUpdate( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		new IpnDAO( form.getUser(), billingId, moduleId ).updateGateRule( contractId, form.getId(), form.getParamInt( "gateId" ), form.getParamInt( "ruleTypeId" ), form.getParam( "rule", "" ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward gateRuleDelete( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		new IpnDAO( form.getUser(), billingId, moduleId ).deleteGateRule( contractId, form.getId() );

		return processJsonForward( conSet, form, response );
	}

	private long ipToLong( String ipAddress )
	{
		long result = 0;

		String[] ipAddressInArray = ipAddress.split( "\\." );
		for( int i = 3; i >= 0; i-- )
		{
			long ip = Long.parseLong( ipAddressInArray[3 - i] );
			result |= ip << (i * 8);
		}

		return result;
	}

	public ActionForward findAddress( ActionMapping mapping, DynActionForm form, HttpServletRequest request, HttpServletResponse response, ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int moduleId = form.getParamInt( "moduleId" );

		long address = ipToLong( form.getParam( "address" ) );
		int mask = form.getParamInt( "mask" );
		int port = form.getParamInt( "port" );
		Date dateFrom = form.getParamDate( "dateFrom" );
		Date dateTo = form.getParamDate( "dateTo" );
		String comment = form.getParam( "comment" );

		form.getResponse().setData( "addresses", new IpnDAO( form.getUser(), billingId, moduleId ).findAddress( form.getPage(), address, mask, port, dateFrom, dateTo, comment ) );

		return processJsonForward( conSet, form, response );
	}
}
