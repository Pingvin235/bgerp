package ru.bgcrm.util;

import ru.bgcrm.plugin.bgbilling.Request;

public class RequestXML extends Request
{
    public RequestXML()
    {
        super();
        setAttribute( "ContentType", "xml" );
    }
}
