package ru.bgcrm.plugin.bgbilling.proto.model.ipn;

import java.util.Date;

import org.w3c.dom.Element;

import ru.bgcrm.util.TimeUtils;

/*<row f0="06.05.2014 16:57:41" f1="открыт" f2="Сервер" f3="Шлюз недоступен - 127.0.0.1 (Manad Common)&#10;"/>*/
public class ContractGateLogItem
{
	private final Date time;
	private final String statusTitle;
	private final String user;
	private final String comment;

	public ContractGateLogItem( Element el )
	{
		this.time = TimeUtils.parse( el.getAttribute( "f0" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS );
		this.statusTitle = el.getAttribute( "f1" );
		this.user = el.getAttribute( "f2" );
		this.comment = el.getAttribute( "f3" );
	}

	public Date getTime()
	{
		return time;
	}

	public String getStatusTitle()
	{
		return statusTitle;
	}

	public String getUser()
	{
		return user;
	}

	public String getComment()
	{
		return comment;
	}
}
