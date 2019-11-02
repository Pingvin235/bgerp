package ru.bgcrm.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.Locker;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.Lock;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class LockAction
	extends BaseAction
{
	public ActionForward add( ActionMapping mapping,
	                           DynActionForm form,
	                           HttpServletRequest request,
	                           HttpServletResponse response,
	                           ConnectionSet conSet )
	    throws BGException
	{
		String lockId = getLockId( form );
		
		Locker.addLock( new Lock( lockId, form.getUserId() ) );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward free( ActionMapping mapping,
	                           DynActionForm form,
	                           HttpServletRequest request,
	                           HttpServletResponse response,
	                           ConnectionSet conSet )
	    throws BGException
	{
		String lockId = getLockId( form );
		
		Locker.freeLock( new Lock( lockId, form.getUserId() ) );		
		
		return processJsonForward( conSet, form, response );		
	}
	
	private String getLockId( DynActionForm form )
    	throws BGIllegalArgumentException
    {
    	String lockId = form.getParam( "lockId" );
    	if( Utils.isBlankString( lockId ) )
    	{
    		throw new BGIllegalArgumentException();
    	}
    	return lockId;
    }
}
