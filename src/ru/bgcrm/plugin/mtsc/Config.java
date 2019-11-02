package ru.bgcrm.plugin.mtsc;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class Config
	extends ru.bgcrm.util.Config
{
	public final String url;
	public final String login;
	public final String password;
	public final String naming;
	
	public Config( ParameterMap setup )
	{
		super( setup );
		
		setup = setup.sub( Plugin.ID + ":" );
		
		url = setup.get( "url", "http://mcommunicator.ru/M2M/m2m_api.asmx/SendMessage" );
		login = setup.get( "login", "BGERP" );
		password = Utils.getDigest( setup.get( "password", "BGERP" ) );
		naming = setup.get( "naming", "BGERP" );
	}

	public String getUrl()
	{
		return url;
	}

	public String getLogin()
	{
		return login;
	}

	public String getPassword()
	{
		return password;
	}

	public String getNaming()
	{
		return naming;
	}
}