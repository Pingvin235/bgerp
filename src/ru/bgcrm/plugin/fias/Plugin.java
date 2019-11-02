package ru.bgcrm.plugin.fias;

import org.w3c.dom.Document;

public class Plugin
	extends ru.bgcrm.plugin.Plugin
{
	public static final String ID = "fias";
	
	public Plugin( Document doc )
	{
		super( doc, ID );
	}
}
