package ru.bgcrm.model.param;

import java.util.Date;
import ru.bgcrm.util.TimeUtils;

public class ParameterLogItem 
{
	private Date date;
	private int userId; 
	private int objectId;
	private int paramId;
	private String text;
	
	public ParameterLogItem( Date date, int id, int userId, int paramId, String text)
	{
		this.objectId = id;
		this.paramId = paramId;
		this.userId = userId;
		this.date = date;
		this.text = text;
	}
	
	public Date getDate()
	{
		return date;
	}
	
	public int getUserId()
	{
		return userId;
	}
	
	public String getText()
	{
		return text;
	}
	
	public int getId()
	{
		return objectId;
	}
	
	public int getParamId()
	{
		return paramId;
	}

	public String getDateFormatted()
	{
		return TimeUtils.format( date, TimeUtils.FORMAT_TYPE_YMDHMS );
	}
}