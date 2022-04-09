package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.model.Pageable;

import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.proto.dao.BillDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.Bill;
import ru.bgcrm.plugin.bgbilling.proto.model.bill.Invoice;
import ru.bgcrm.plugin.bgbilling.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class BillAction
	extends BaseAction
{
	public ActionForward attributeList( ActionMapping mapping,
	                                    DynActionForm form,
	                                    HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		form.getResponse().setData( "list", new BillDAO( form.getUser(), billingId, moduleId ).getAttributeList( contractId ) );

		return processUserTypedForward( conSet, mapping, form, response, "attributeList" );
	}

	public ActionForward docTypeList( ActionMapping mapping,
	                                  DynActionForm form,
	                                  HttpServletRequest request,
	                                  HttpServletResponse response,
	                                  ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		BillDAO billDao = new BillDAO( form.getUser(), billingId, moduleId );
		form.getResponse().setData( "billTypeList", billDao.getContractDocTypeList( contractId, "bill" ) );
		form.getResponse().setData( "invoiceTypeList", billDao.getContractDocTypeList( contractId, "invoice" ) );

		return processUserTypedForward( conSet, mapping, form, response, "docTypeList" );
	}

	public ActionForward docTypeAdd( ActionMapping mapping,
	                                  DynActionForm form,
	                                  HttpServletRequest request,
	                                  HttpServletResponse response,
	                                  ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		new BillDAO( form.getUser(), billingId, moduleId ).contractDocTypeAdd( contractId, form.getParam( "typeIds" ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward docTypeDelete( ActionMapping mapping,
	                                    DynActionForm form,
	                                    HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );

		new BillDAO( form.getUser(), billingId, moduleId ).contractDocTypeDelete( contractId, form.getParam( "typeIds" ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward documentList( ActionMapping mapping,
	                                   DynActionForm form,
	                                   HttpServletRequest request,
	                                   HttpServletResponse response,
	                                   ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int contractId = form.getParamInt( "contractId" );
		int moduleId = form.getParamInt( "moduleId" );
		String mode = form.getParam( "mode", "bill" );
		form.setParam( "mode", mode );

		BillDAO billDao = new BillDAO( form.getUser(), billingId, moduleId );
		if( "bill".equals( mode ) )
		{
			billDao.searchBillList( contractId, new Pageable<Bill>( form ) );
		}
		else
		{
			billDao.searchInvoiceList( contractId, new Pageable<Invoice>( form ) );
		}

		return processUserTypedForward( conSet, mapping, form, response, "documentList" );
	}

	public ActionForward getDocument( ActionMapping mapping,
	                                  DynActionForm form,
	                                  HttpServletRequest request,
	                                  HttpServletResponse response,
	                                  ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int moduleId = form.getParamInt( "moduleId" );
		String type = form.getParam( "type" );
		String ids = form.getParam( "ids" );

		try
		{
			OutputStream out = response.getOutputStream();
			Utils.setFileNameHeaders( response, type + ".pdf" );
			out.write( new BillDAO( form.getUser(), billingId, moduleId ).getDocumentsPdf( ids, type ) );
		}
		catch( Exception ex )
		{
			throw new BGException( ex );
		}

		return null;
	}

	public ActionForward setPayed( ActionMapping mapping,
	                                  DynActionForm form,
	                                  HttpServletRequest request,
	                                  HttpServletResponse response,
	                                  ConnectionSet conSet )
		throws BGException
	{
		String billingId = form.getParam( "billingId" );
		int moduleId = form.getParamInt( "moduleId" );

		String ids = form.getParam( "ids" );
		Date date = form.getParamDate( "date" );
		BigDecimal summa = Utils.parseBigDecimal( form.getParam( "summa" ) );
		String comment = form.getParam( "comment" );

		BillDAO billDao = new BillDAO( form.getUser(), billingId, moduleId );
		if( date != null )
		{
			billDao.setPayed( ids, true, date, summa, comment );
		}
		else
		{
			billDao.setPayed( ids, false, null, null, null );
		}


		return processJsonForward( conSet, form, response );
	}
}