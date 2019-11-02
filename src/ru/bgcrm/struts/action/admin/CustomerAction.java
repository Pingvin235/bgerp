package ru.bgcrm.struts.action.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.CustomerGroupCache;
import ru.bgcrm.dao.CustomerGroupDAO;
import ru.bgcrm.model.CustomerGroup;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class CustomerAction
	extends BaseAction
{
	public CustomerAction()
	{
		super();
	}

	public ActionForward groupList( ActionMapping mapping,
									DynActionForm form,
									HttpServletRequest request,
									HttpServletResponse response,
									ConnectionSet conSet )
		throws Exception
	{
		CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO( conSet.getConnection() );
		customerGroupDAO.searchGroup( new SearchResult<CustomerGroup>( form ) );

		return mapping.findForward( "groupList" );
	}

	public ActionForward groupGet( ActionMapping mapping,
								   DynActionForm form,
								   HttpServletRequest request,
								   HttpServletResponse response,
								   ConnectionSet conSet )
		throws Exception
	{
		CustomerGroup group = new CustomerGroupDAO( conSet.getConnection() ).getGroupById( form.getId() );
		if( group != null )
		{
			form.getResponse().setData( "group", group );
		}

		return processUserTypedForward( conSet, mapping, form, response, "groupUpdate" );
	}

	public ActionForward groupUpdate( ActionMapping mapping,
									  DynActionForm form,
									  HttpServletRequest request,
									  HttpServletResponse response,
									  ConnectionSet conSet )
		throws Exception
	{
		CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO( conSet.getConnection() );

		CustomerGroup group = new CustomerGroup();
		group.setId( form.getId() );
		group.setTitle( form.getParam( "title", "" ) );
		group.setComment( form.getParam( "comment", "" ) );

		customerGroupDAO.updateGroup( group );

		CustomerGroupCache.flush( conSet.getConnection() );

		return processJsonForward( conSet, form, response );
	}

	public ActionForward groupDelete( ActionMapping mapping,
	                                  DynActionForm form,
	                                  HttpServletRequest request,
	                                  HttpServletResponse response,
	                                  ConnectionSet conSet )
		throws Exception
	{
		CustomerGroupDAO customerGroupDAO = new CustomerGroupDAO( conSet.getConnection() );
		customerGroupDAO.deleteGroup( form.getId() );

		CustomerGroupCache.flush( conSet.getConnection() );

		return processJsonForward( conSet, form, response );
	}
}