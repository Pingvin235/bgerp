package ru.bgcrm.event.client;

@Deprecated
public class UrlOpenEvent
	extends ClientEvent
{
	private final String url;
	
	public UrlOpenEvent( String url )
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}
}
