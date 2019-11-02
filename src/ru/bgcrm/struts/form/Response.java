package ru.bgcrm.struts.form;

import ru.bgcrm.event.client.ClientEvent;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response
{
	public static final String STATUS_OK = "ok";
	public static final String STATUS_ERROR = "error";
	public static final String STATUS_MESSAGE = "message";

	private String status = STATUS_OK;
	private String message = "";

	// данные, которые передаются в ответе
	private Map<String, Object> data = new HashMap<String, Object>();
	// события для обработки клиентом, в т.ч. изменившиеся с последнего действия справочники
	private List<ClientEvent> eventList = new ArrayList<ClientEvent>();

	public String getStatus()
	{
		return status;
	}

	public void setStatus( String status )
	{
		this.status = status;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public List<ClientEvent> getEventList()
	{
		return eventList;
	}

	public void addEvent( ClientEvent event )
	{
		if( event != null )
		{
			eventList.add( event );
		}
	}

	public Map<String, Object> getData()
	{
		return data;
	}

	public void setData( String key, Object data )
	{
		this.data.put( key, data );
	}

	public void addSearchResult( SearchResult<?> result )
	{
		Page page = result.getPage();

		if( page.isNeedPaging() )
		{
			this.data.put( "page", page );
		}

		this.data.put( "list", result.getList() );
	}
}
