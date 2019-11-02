package ru.bgcrm.event;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Событие генерируется после изменения параметра.
 * В базе значение параметра уже изменено.
 */
public class ParamChangedEvent
    extends UserEvent
{
	private Parameter parameter;
	private int objectId;
	private Object value;
	
	public ParamChangedEvent( DynActionForm form, Parameter parameter, int objectId, Object value )
    {
	    super( form );
	    
	    this.parameter = parameter;
	    this.objectId = objectId;
	    this.value = value;
    }
	
	public Parameter getParameter()
    {
    	return parameter;
    }
	
	public int getObjectId()
    {
    	return objectId;
    }

	public Object getValue()
    {
    	return value;
    }
}