package ru.bgcrm.plugin.document.docgen;

import ru.bgcrm.dao.CustomerDAO;
import ru.bgcrm.event.Event;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.Customer;
import ru.bgcrm.util.sql.ConnectionSet;

/**
 * Убрать, когда перестанут использовать иные кроме CommonDocumentGenerator генераторы.
 */
@Deprecated
public class CustomerDocumentGenerator
    extends CommonDocumentGenerator
{
	private Customer customer;
	
	public CustomerDocumentGenerator()
    {
	    super();
    }
	
	public CustomerDocumentGenerator( Event e, ConnectionSet conSet )
    {
	    super( e, conSet );
    }

	@Override
    public String processMacros( String incomingValue, String macros )
		throws BGException		
    {
		if( macros.equals( "title" ) )
	    {
	    	if( customer == null )
	    	{
	    		customer = new CustomerDAO( conSet.getConnection() ).getCustomerById( event.getObjectId() );
	    	}	    	
	    	if( customer != null )
	    	{
	    		return customer.getTitle();
	    	}
	    	return "";
	    }
	    else
	    {		
	    	return super.processMacros( incomingValue, macros );
	    }
    }
}