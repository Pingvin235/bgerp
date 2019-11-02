package ru.bgcrm.model.process.queue;

import org.apache.commons.lang.StringUtils;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class FilterStatusUser
	extends Filter
{
	private String statusTitle;
	private int statusId;

	public FilterStatusUser( int id, ParameterMap filter, String type )
		throws BGException
	{
		super( id, filter );
		
		int statusId = Utils.parseInt( StringUtils.substringAfter( type, ":" ) );
		Status status = ProcessTypeCache.getStatusMap().get( statusId );

		if( status != null )
		{
			this.statusTitle = status.getTitle();
			this.statusId = status.getId();
		}
		else
		{
			throw new BGException( "Incorrect status: " + statusId );
		}
	}

	public String getStatusTitle()
	{
		return statusTitle;
	}

	public int getStatusId()
	{
		return statusId;
	}
}
