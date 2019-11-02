package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Pair;
import ru.bgcrm.plugin.bgbilling.proto.dao.CashCheckDAO;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class CashCheckAction
	extends ContractAction
{
	public ActionForward registratorList( ActionMapping mapping,
										  DynActionForm form,
										  HttpServletRequest request,
										  HttpServletResponse response,
										  ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		CashCheckDAO cashCkeckDao = new CashCheckDAO( form.getUser(), billingId );

		form.getResponse().setData( "registratorList", cashCkeckDao.getRegistratorList() );

		return processJsonForward( conSet, form, response );
	}
	
	public ActionForward printCheck( ActionMapping mapping,
									 DynActionForm form,
									 HttpServletRequest request,
									 HttpServletResponse response,
									 ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int paymentId = form.getParamInt( "paymentId" );

		CashCheckDAO cashCkeckDao = new CashCheckDAO( form.getUser(), billingId );
		Pair<String, String> result = cashCkeckDao.printCheck( form.getParamInt( "selectedRegisterId" ), paymentId, form.getParam( "clientCash" ), form.getParam( "selectedRegisterPswd" ) );

		form.getResponse().setData( "summa", result.getFirst() );
		form.getResponse().setData( "submit", result.getSecond() );

		return processJsonForward( conSet, form, response );
	}
}