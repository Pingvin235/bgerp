package ru.bgcrm.event;

import java.util.List;
import java.util.Map;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.struts.form.DynActionForm;

@Deprecated
public class ParamListGetEvent
	extends UserEvent
{
	private int objectId;
	private String object;
	private Parameter parameter;
	private Map<Integer, String> values;
	private List<IdTitle> listValues;

	public ParamListGetEvent( DynActionForm form, String object, int objectId, Parameter parameter, Map<Integer, String> values, List<IdTitle> listValues )
	{
		super( form );

		this.listValues = listValues;
		this.objectId = objectId;
		this.parameter = parameter;
		this.values = values;
		this.object = object;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public Parameter getParameter()
	{
		return parameter;
	}

	public Map<Integer, String> getValues()
	{
		return values;
	}

	public List<IdTitle> getListValues()
	{
		return listValues;
	}

	public String getObject()
	{
		return object;
	}
}
