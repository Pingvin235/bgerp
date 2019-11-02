package ru.bgcrm.event.client;

/**
 * Сообщение о том, что параметры объекта изменились - 
 * необходимо перечитать таблицу параметров объекта.
 */
public class ParametersChangedEvent
	extends ClientEvent
{
	private final String objectType;
	private final int objectId;
	
	public ParametersChangedEvent( String objectType, int objectId )
	{
		this.objectType = objectType;
		this.objectId = objectId;
	}

	public String getObjectType()
	{
		return objectType;
	}

	public int getObjectId()
	{
		return objectId;
	}
}
