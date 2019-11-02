package ru.bgcrm.plugin.document;

import org.w3c.dom.Document;

public class Plugin
    extends ru.bgcrm.plugin.Plugin
{
	public static final String ID = "document";
	
	public Plugin( Document doc )
    {
	    super( doc, ID );
    }
}
