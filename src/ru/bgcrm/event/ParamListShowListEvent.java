package ru.bgcrm.event;

import ru.bgcrm.model.param.ParameterValuePair;
import ru.bgcrm.struts.form.DynActionForm;

@Deprecated
public class ParamListShowListEvent
	extends UserEvent
{
	private ParameterValuePair parameterValuePair;
	private int objectId;
	private String objectType;

	public ParamListShowListEvent( DynActionForm form, ParameterValuePair parameterValuePair, int objectId, String objectType )
	{
		super( form );

		this.objectId = objectId;
		this.objectType = objectType;
		this.parameterValuePair = parameterValuePair;
	}

	public ParameterValuePair getParameterValuePair()
	{
		return parameterValuePair;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public String getObjectType()
	{
		return objectType;
	}
}
