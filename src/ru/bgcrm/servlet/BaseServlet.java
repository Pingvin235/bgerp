package ru.bgcrm.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import ru.bgcrm.util.Setup;

public class BaseServlet
    extends HttpServlet
{
	protected final Logger log;
	protected Setup setup;
	private Class<?> clazz;

	protected BaseServlet( Class<?> clazz )
	{
		super();
		this.clazz = clazz;
        log = Logger.getLogger( clazz );
        setup = Setup.getSetup();
	}

	protected void putDocumentToResponse( HttpServletResponse response,
	                                      Document document )
	    throws TransformerFactoryConfigurationError
	{
		response.setContentType( "text/xml" );
		// переносим данные в объект response
		try
		{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource( document );
			StreamResult result = new StreamResult( response.getOutputStream() );
			transformer.transform( source, result );
		}
		catch( Exception ex )
		{
			log.error( clazz.getName() +  ".putDocumentToResponse", ex );
		}
	}
}
