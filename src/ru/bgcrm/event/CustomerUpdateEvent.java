package ru.bgcrm.event;

import ru.bgcrm.struts.form.DynActionForm;

public class CustomerUpdateEvent
	extends UserEvent
{
	private int customerId;

	public CustomerUpdateEvent( DynActionForm form, int customerId )
	{
		super( form );
		this.customerId = customerId;
	}

	public int getCustomerId()
	{
		return customerId;
	}
}
