package ru.bgcrm.plugin.mobile.struts.action.open;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.model.BGException;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class CustomerAction
	extends BaseAction
{
	public ActionForward state( ActionMapping mapping, DynActionForm form, ConnectionSet conSet )
		throws BGException
	{
		//dd	
				
		return status( conSet, form );
	}	
	
}
