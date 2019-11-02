package ru.bgcrm.event;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.struts.form.DynActionForm;

/**
 * Событие генерируется перед изменением параметра, 
 * при этом в базе ещё старое значение параметра.
 */
public class ParamChangingEvent
	extends UserEvent
{
	private Parameter parameter;
	private int objectId;
	private Object value;

	public ParamChangingEvent( DynActionForm form, Parameter parameter, int objectId, Object value )
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