package ru.bgcrm.plugin.bgbilling.proto.struts.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.bgcrm.model.BGException;
import ru.bgcrm.plugin.bgbilling.proto.dao.PhoneDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.phone.PhoneResourceItem;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

public class PhoneAction
	extends BaseAction
{
	public ActionForward updateClientItem( ActionMapping mapping,
										   DynActionForm form,
										   HttpServletRequest request,
										   HttpServletResponse response,
										   ConnectionSet conSet )
		throws BGException
	{
		PhoneDAO phoneDAO = new PhoneDAO( form.getUser(), form.getParam( "billingId" ) );

		phoneDAO.updateClientItem( form.getParamInt( "contractId" ),
								   form.getParam( "clientsNumbers" ),
								   form.getParam( "alias" ),
								   form.getParamInt( "sourceId" ),
								   new Date(),
								   null,
								   form.getParamInt( "id", 0 ),
								   form.getParam( "comment" ),
								   form.getParamInt( "objectId" ),
								   form.getParamInt( "type", 1 ) );

		return processUserTypedForward( conSet, mapping, form, response, FORWARD_DEFAULT );
	}

	public ActionForward sessionList( ActionMapping mapping,
									  DynActionForm form,
									  HttpServletRequest request,
									  HttpServletResponse response,
									  ConnectionSet conSet )
		throws BGException
	{
		PhoneDAO phoneDAO = new PhoneDAO( form.getUser(), form.getParam( "billingId" ) );

		form.getResponse().setData( "sessionList", phoneDAO.getPhoneSessionList( form.getParamInt( "contractId" ), form.getParamInt( "pointId" ), form.getParamInt( "days" ) ) );

		return processUserTypedForward( conSet, mapping, form, response, "sessionList" );
	}
	
	public ActionForward getFreeNumberResourceList( ActionMapping mapping, 
	                                                DynActionForm form,
	                                                HttpServletRequest request,
	                                                HttpServletResponse response,
	                                                ConnectionSet conSet
	                                                )
        throws BGException
	{
		int categoryId = form.getParamInt( "categoryId", -1 );
		if( categoryId == -1 )
		{
			throw new BGException( "Не указан код категории ресурса в параметре запроса categoryId" );
		}
		Date date = form.getParamDate( "date", new Date() );
		List<String> freePhoneList = new PhoneDAO( form.getUser(), request.getParameter( "billingId" ) ).getFreeNumberResourceList( categoryId, date );
		form.getResponse().setData( "freePhoneList", freePhoneList );
		return processJsonForward( conSet, form, response );
	}

    public ActionForward getPhoneResourceList( ActionMapping mapping,
                                                DynActionForm form,
                                                HttpServletRequest request,
                                                HttpServletResponse response,
                                                ConnectionSet conSet )
        throws BGException
    {
        int categoryId = form.getParamInt( "categoryId", -1 );
        Date onDate = form.getParamDate("onDate", new Date());
        String fromNumber = form.getParam("fromNumber");
        String toNumber = form.getParam( "toNumber" );
        String status = form.getParam( "status" );

        List<PhoneResourceItem> phoneResourceList = new PhoneDAO( form.getUser(), request.getParameter( "billingId" ) ).
                getPhoneResourceTable( categoryId, fromNumber, toNumber, status, onDate );

        form.getResponse().setData( "phoneResourceList", phoneResourceList );

        return processJsonForward( conSet, form, response );
    }
}
