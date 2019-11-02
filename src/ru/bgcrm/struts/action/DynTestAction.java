package ru.bgcrm.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.struts.form.DynActionForm;

public class DynTestAction
    extends Action
{
	@Override
    public ActionForward execute( ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response )
        throws Exception
    {
		DynActionForm actionForm = (DynActionForm)form;
		
		/*System.out.println( actionForm.get( "param1" ) );
		System.out.println( actionForm.get( "param2" ) );*/
		
		/*System.out.println( ((String[])actionForm.get( "param2" ))[0] );
		System.out.println( ((String[])actionForm.get( "param2" ))[1] );*/
		
		System.out.println( "Action: " + actionForm.getAction() );
		System.out.println( "Page: " + actionForm.getPage().getRecordCount() );
		System.out.println( "Param1: " + actionForm.getParam( "param1" ) );
		System.out.println( "Param2: " + actionForm.getParamArray( "param2" )[1] );
		
		request.setAttribute( "form", form );
		
	    return mapping.findForward( "default" );
    }	
}
