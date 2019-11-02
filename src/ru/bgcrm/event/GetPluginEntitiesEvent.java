package ru.bgcrm.event;

import ru.bgcrm.struts.form.DynActionForm;

public class GetPluginEntitiesEvent
    extends UserEvent
{
	public GetPluginEntitiesEvent( DynActionForm user )
    {
	    super( user );
    }
}
