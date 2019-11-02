package ru.bgcrm.plugin.bgbilling.struts.form;

import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

import ru.bgcrm.model.ArrayHashMap;
import ru.bgcrm.struts.form.DynActionForm;

public class BillingActionForm
    extends DynActionForm
{
	// изолированный набор параметров
	private ArrayHashMap billing = new ArrayHashMap();

	public ArrayHashMap getBilling()
    {
    	return billing;
    }
	
	private static DynaClass dynaClass = new BillingActionForm();

	@Override
	public DynaClass getDynaClass()
	{
		return dynaClass;
	}
	
	private static final DynaProperty billingProperty  = new DynaProperty( "billing", ArrayHashMap.class );
	
	@Override
	public DynaProperty getDynaProperty( String name )
	{
		if( billing.equals( name ) )
		{
			return billingProperty;
		}
		return super.getDynaProperty( name );
	}

	@Override
    public void set( String name, String key, Object value )
    {
	   billing.put( key, value );
    }	
}
