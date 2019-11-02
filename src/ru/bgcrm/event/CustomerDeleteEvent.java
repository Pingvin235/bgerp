package ru.bgcrm.event;

import ru.bgcrm.struts.form.DynActionForm;

public class CustomerDeleteEvent
    extends UserEvent
{
	private int customerId;
	
	public CustomerDeleteEvent( DynActionForm form, int customerId )
	{
		super( form );
		this.customerId = customerId;
	}

	public int getCustomerId()
    {
    	return customerId;
    }
}
