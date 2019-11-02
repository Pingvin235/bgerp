package ru.bgcrm.plugin.bgbilling.struts.action;

import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamResult;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Document;

import ru.bgcrm.model.ArrayHashMap;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.TransferData;
import ru.bgcrm.plugin.bgbilling.dao.BGBillingDAO;
import ru.bgcrm.plugin.bgbilling.struts.form.BillingActionForm;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

@Deprecated
public class BillingAction
    extends BaseAction
{
	@Override
    protected ActionForward unspecified( ActionMapping mapping,
                                         DynActionForm actionForm,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         Connection con )
        throws Exception
    {
		BillingActionForm form = (BillingActionForm)actionForm;
		
		Document doc = null;
		
		if( log.isDebugEnabled() )
		{
			log.debug( "r:" + request + "; f:" + form + "; q: " + request.getQueryString() );
		}
		
		String billingId = form.getParam( "billingId" );
		if( Utils.notBlankString( billingId )  )
		{
			BGBillingDAO billingDao = new BGBillingDAO();

			ArrayHashMap reqParams = form.getBilling();
			
			Request req = new Request();
			// вместо putAll, т.к. там лежат массивы из строк
			for( String key : reqParams.keySet() )
			{
				req.setAttribute( key, reqParams.get( key ) );
			}
			
			// акшен вызван только для попадания в JSPшку
			if( reqParams.containsKey( "action" ) )
			{
				doc = billingDao.doRequestToBilling( billingId, form.getUser(), req );
			}
		}
		else
		{
			doc = TransferData.createDocWithError( "Не указан биллинг." );
		}
		
		if( Utils.notBlankString( form.getForwardFile() ) )
		{
			request.setAttribute( "dataDoc", doc );
			return new ActionForward( form.getForwardFile() );
		}
		else
		{
			final String charset = Utils.UTF8.name();
			response.setContentType( "text/xml; charset='" + charset + ";" );
			response.setHeader( "cache-control", "no-cache, no-store" );
			
			XMLUtils.serialize( doc, new StreamResult( response.getOutputStream() ), charset, false );			
	
			return null;
		}
	}
}