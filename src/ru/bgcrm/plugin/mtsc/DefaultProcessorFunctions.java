package ru.bgcrm.plugin.mtsc;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import ru.bgcrm.dao.expression.ExpressionBasedFunction;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

public class DefaultProcessorFunctions
	extends ExpressionBasedFunction
{
	private static final Logger log = Logger.getLogger( DefaultProcessorFunctions.class );
	
	public DefaultProcessorFunctions()
	{}
	
	/**
	 * Отправка СМС сообщения.
	 * @param numberTo номер получателя
	 * @param text текст сообщения
	 * @throws BGException
	 */
	public void sendSms(String numberTo, String text)
		throws BGException
	{
		Config config = Setup.getSetup().getConfig( Config.class );
		if( Utils.isBlankString( config.getUrl() ) )
		{
			return;
		}
		
		try
		{
			URIBuilder url = new URIBuilder( config.getUrl() );
			url.addParameter( "login", config.getLogin() );
			url.addParameter( "password", config.getPassword() );
			url.addParameter( "naming", config.getNaming() );
			url.addParameter( "msid", numberTo );
			url.addParameter( "message", text );
    			
			Request req = Request.Get(url.build());
			if (log.isDebugEnabled()) 
			{
				log.debug( "Sending: " + req  );
			}
    		String response = req.execute().returnContent().asString( Utils.UTF8 );
			if (log.isDebugEnabled())
			{
				log.debug( "=> " + response );
			}
		}
		catch( Exception e )
		{
			log.error( e.getMessage(), e );
		}		
	}
}
