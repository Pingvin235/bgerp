package ru.bgcrm.worker;

import java.sql.Connection;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.SphinxDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;

public class InsertUnaccountedCustomers
	implements Runnable
{
	private static Logger logger = Logger.getLogger( SphinxCache.class );

	@Override
	public void run()
	{
		Connection con = null;
		try
		{
			con = Setup.getSetup().getDBConnectionFromPool();
			SphinxDAO sphinxDAO = new SphinxDAO( con );
			sphinxDAO.insertUnaccountedCusomers();
			SQLUtils.commitConnection( con );
		}
		catch( BGException e )
		{
			logger.error( e );
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}
	}
	
	public static void main(String[] args)
	{
		new InsertUnaccountedCustomers().run();
	}
}
