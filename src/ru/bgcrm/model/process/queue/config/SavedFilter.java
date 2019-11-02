package ru.bgcrm.model.process.queue.config;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;

/**
 * Фильтр очереди, доступный другим пользователям.
 */
public class SavedFilter
	extends IdTitle
{
	private final int queueId;
	private final String url;

	public SavedFilter( int id, ParameterMap config )
	{
		this.id = id;
		this.queueId = config.getInt( "queueId", 0 );
		this.title = config.get( "title" );
		this.url = config.get( "url" );
	}
	
	public SavedFilter( int queueId, int id, String title, String url )
	{
		super( id, title );
		this.queueId = queueId;
		this.title = title;
		this.url = url;
	}

	public int getQueueId()
	{
		return queueId;
	}

	public String getUrl()
	{
		return url;
	}
}
