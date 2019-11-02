package ru.bgcrm.plugin.dispatch;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.dispatch.dao.DispatchDAO;
import ru.bgcrm.plugin.dispatch.model.DispatchMessage;
import ru.bgcrm.util.MailMsg;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class Sender
	implements Runnable
{
	private static final Logger log = Logger.getLogger( Sender.class );
	
	@Override
	public void run()
	{
		try
		{
			List<DispatchMessage> messageList = null; 
			
			Connection conSlave = null;
			try
			{
				conSlave = Setup.getSetup().getConnectionPool().getDBSlaveConnectionFromPool();
				
				SearchResult<DispatchMessage> result = new SearchResult<DispatchMessage>();
				new DispatchDAO( conSlave ).messageSearch( result, false );
				
				messageList = result.getList();
			}
			finally
			{
				SQLUtils.closeConnection( conSlave );
			}
			
			Connection con = null;
			
			if( messageList.size() > 0 )
			{
				log.info( "Found " + messageList.size() + " dispatch messages for send.." );
				
				for( DispatchMessage message : messageList )
				{
					List<String> accounts = null;
					
					try
					{
						conSlave = Setup.getSetup().getConnectionPool().getDBSlaveConnectionFromPool();
						accounts = new DispatchDAO( conSlave ).messageAccountList( message.getId() );
					}
					finally
					{
						SQLUtils.closeConnection( conSlave );
					}
					
					//TODO: Возможно, стоит впоследствии разнести в разные потоки, если почтовик разрешит параллельную отправку..
					for( String account : accounts )
					{
						new MailMsg( Setup.getSetup() ).sendMessage( account, message.getTitle(), message.getText() );
					}
					
					try
					{
						con = Setup.getSetup().getDBConnectionFromPool();
						message.setSentTime( new Date() );
						new DispatchDAO( con ).messageUpdate( message );
					}
					finally
					{
						SQLUtils.closeConnection( con );
					}
				}
			}
		}
		catch( Exception e )
		{
    		log.error( e.getMessage(), e );
		}				
	}
}
