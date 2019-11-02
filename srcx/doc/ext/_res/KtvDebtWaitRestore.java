package ru.bgcrm.dyn.sofit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;


import org.apache.log4j.Logger;

import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.user.User;

import static ru.bgcrm.dyn.sofit.KtvDebtManager.*;
import static ru.bgcrm.dao.process.Tables.*;
import static ru.bgcrm.dao.Tables.*;

public class KtvDebtWaitRestore
	implements Runnable
{
	private static final Logger log = Logger.getLogger( KtvDebtWaitRestore.class );

	@Override
	public void run()
	{
		Connection con = null;
		try
		{
			con = Setup.getSetup().getDBConnectionFromPool();
			
			String query = 
				"SELECT process.* FROM " + TABLE_PROCESS + " AS process " +
				"INNER JOIN " + TABLE_PARAM_DATE + " AS pd ON process.id=pd.id AND pd.param_id=? AND pd.value<=CURDATE() " +
				"WHERE close_dt IS NULL AND type_id=? ";
			PreparedStatement ps = con.prepareStatement( query );
			ps.setInt( 1, PROCESS_PARAM_DATE_RECALL );
			ps.setInt( 2, PROCESS_TYPE_DEBTOR );			
			
			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				Process process = ProcessDAO.getProcessFromRs( rs );
				
				log.info( "Opening debt process: " + process.getId() );
				
				StatusChange change = new StatusChange();
				change.setProcessId( process.getId() );
				change.setDate( new Date() );
				change.setStatusId( PROCESS_STATUS_OPEN );
				change.setUserId( User.USER_SYSTEM_ID );
				change.setComment( "Настала дата повторного обзвона" );
								
				ProcessAction.processStatusUpdate( DynActionForm.SERVER_FORM, con, process, change );
				
				con.commit();
			}
			ps.close();
		}
		catch( Exception e )
		{
			log.error( e.getMessage(), e );
		}
		finally
		{
			SQLUtils.closeConnection( con );
		}
	}

}
