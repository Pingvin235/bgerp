package ru.bgcrm.servlet;

import java.io.File;

import javax.servlet.ServletException;

/**
 * Переопределённый ActionServlet, подгружает для Struts конфигурации плагинов из файлов вида struts-config-<module>.xml.
 */
public class ActionServlet
    extends org.apache.struts.action.ActionServlet
{
	@Override
    public void init()
        throws ServletException
    {
		final String configPrefix = "struts-config";
		
	    StringBuilder paths = new StringBuilder();
		
	    File webInf = new File( "webapps/WEB-INF" );
		for( File file : webInf.listFiles() )
		{
			String fileName = file.getName();
			
			if( !fileName.startsWith( configPrefix ) )
			{
				continue;
			}
			
			if( paths.length() > 0 )
			{
				paths.append( "," );
			}
			
			paths.append( "/WEB-INF/" + fileName );
		}
		
		this.config = paths.toString();
	    
		super.init();
    }
}