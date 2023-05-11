package ru.bgcrm.dyn.sofit;

import static ru.bgcrm.dao.process.Tables.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.UserEvent;
import ru.bgcrm.event.listener.EventListener;
import ru.bgcrm.event.process.ProcessChangedEvent;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.StatusChange;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.proto.dao.ContractParamDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.struts.action.LinkAction;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.PreparedDelay;
import ru.bgcrm.util.sql.SQLUtils;

public class KtvDebtManager
	implements Runnable, EventListener<UserEvent>
{
	private static final Logger log = Logger.getLogger( KtvDebtManager.class );
	
	protected static final int PROCESS_TYPE_DEBTOR = 4;
	protected static final int PROCESS_TYPE_DISCONNECT = 2;
	
	protected static final int PROCESS_STATUS_OPEN = 2;
	protected static final int PROCESS_STATUS_ACCEPT = 3;
	protected static final int PROCESS_STATUS_CLOSE = 1;
	protected static final int PROCESS_STATUS_DISCONNECT = 6;
	protected static final int PROCESS_STATUS_PAY_WAIT = 5;
	
	protected static final int PROCESS_PARAM_ADDRESS = 1;
	protected static final int PROCESS_PARAM_DATE_RECALL = 2;
	
	//protected static final int PARAM_BILLING_ADDRESS = 35;
	protected static final int PARAM_BILLING_PHONE = 12;
	protected static final int PARAM_BILLING_FIO = 10;
	
	protected static final User BILLING_USER = new User( "bitel", "bgbilling@bitel.ru" );
	
	private DBInfo dbInfo;
	
	private static AtomicBoolean working = new AtomicBoolean(); 
	
	@Override
	public void run()
	{
		log.info( "Started" );
		
		if( working.get() )
		{
			log.warn( "Already working" );
			return;
		}
		
		Connection con = Setup.getSetup().getDBConnectionFromPool();
		Connection conBilling = null;
		try
		{
			working.set( true );
			
			dbInfo = Utils.getFirst( DBInfoManager.getInstance().getDbInfoList() );
			if( dbInfo == null )
			{
				throw new BGException( "DBInfo not found" );
			}
			
			conBilling = dbInfo.getConnectionPool().getDBConnectionFromPool();
			
			Map<Integer, Process> contractDebtProcesses = new HashMap<Integer, Process>(); 
			
			// процессы "Должник"
			String query = 
			 	"SELECT pl.object_id, process.*  FROM " + TABLE_PROCESS + " AS process " +
				"INNER JOIN " + TABLE_PROCESS_LINK + " AS pl ON process.id=pl.process_id AND pl.object_type=? " +
				"WHERE process.type_id=? AND process.close_dt IS NULL";  
			PreparedStatement ps = con.prepareStatement( query );
			ps.setString( 1, Contract.OBJECT_TYPE + ":" + dbInfo.getId() );
			ps.setInt( 2, PROCESS_TYPE_DEBTOR );
			
			ResultSet rs = ps.executeQuery();
			while( rs.next() )
			{
				contractDebtProcesses.put( rs.getInt( 1 ), ProcessDAO.getProcessFromRs( rs ) );
			}
			ps.close();
			
			query = "DROP TABLE IF EXISTS bgcrmtmp.balance_dump";
			conBilling.createStatement().executeUpdate( query );
			
			query = 
				"CREATE TEMPORARY TABLE bgcrmtmp.balance_dump( UNIQUE(cid) ) " +
				"SELECT cid, MAX(yy*12+(mm-1))%12 + 1 AS mm, " +
				"FLOOR(MAX(yy*12+(mm-1)) / 12) AS yy " + 
				"FROM contract_balance GROUP BY cid";
			conBilling.createStatement().executeUpdate( query );
			
			// -------------------------------------
			// условия выборки договоров - должников
			
			// базовый - код группы 25 - сумма долга 744 руб
			processContracts( getContracts( conBilling, 1L<<25, null, new BigDecimal( -744 ) ), contractDebtProcesses, con );			
			// код тарифа - 26, долг - 720 и выше
			processContracts( getContracts( conBilling, null, 26, new BigDecimal( -720 ) ), contractDebtProcesses, con );
			
			// не должники
			for( Process process : contractDebtProcesses.values() )
			{
				log.info( "Closing debt process: " + process.getId() );
				
				if( process.getStatusId() == PROCESS_STATUS_OPEN ||
					process.getStatusId() == PROCESS_STATUS_ACCEPT )
				{				
    				StatusChange change = new StatusChange();
    				change.setProcessId( process.getId() );
    				change.setStatusId( PROCESS_STATUS_CLOSE );
    				change.setDate( new Date() );
    				change.setUserId( User.USER_SYSTEM_ID );
    				change.setComment( "Более не является должником" );
    				
    				ProcessAction.processStatusUpdate( DynActionForm.SERVER_FORM, con, process, change );
				}
				else
				{
					// Статус "Отключение".
				}
			}				
		}
		catch( Exception e )
		{
			log.error( e.getMessage(), e );
		}
		finally
		{
			SQLUtils.closeConnection( con, conBilling );
			working.set( false );
		}
		
		log.info( "Finished" );
	}


	private PreparedDelay getContracts( Connection conBilling, Long groupFilter, Integer tariffFilter, BigDecimal restFilter )
		throws SQLException
	{
		PreparedDelay pd = new PreparedDelay( conBilling )	;
		
		String query = 
			"SELECT contract.id, contract.title, contract.gr, (balance.summa1+balance.summa2-balance.summa3-balance.summa4) AS rest " +
			"FROM contract " +
			"INNER JOIN bgcrmtmp.balance_dump AS dump ON contract.id=dump.cid " +
			"INNER JOIN contract_balance AS balance ON dump.cid=balance.cid AND dump.yy=balance.yy AND dump.mm=balance.mm ";
		pd.addQuery( query );
		
		if( tariffFilter != null )
		{
			pd.addQuery(  "INNER JOIN contract_tariff AS tariff ON contract.id=tariff.cid AND tariff.date2 IS NULL AND tariff.tpid=? " );
			pd.addInt( tariffFilter );
		}			
			
		pd.addQuery( "WHERE contract.status=0 AND contract.date2 IS NULL " );
		// тестовый договор
		pd.addQuery( " AND contract.id=108035" );		
		
		if( groupFilter != null )
		{
			pd.addQuery( " AND (contract.gr&?)>0" );
			pd.addLong( groupFilter );
		}
		if( restFilter != null )
		{
			pd.addQuery( " HAVING rest<?" );
			pd.addBigDecimal( restFilter );
		}
		
		if( log.isDebugEnabled() )
		{
			log.debug( "Query: " + pd.getQuery() );
		}
		
		return pd;
	}
	
	private void processContracts( PreparedDelay pd, Map<Integer, Process> contractDebtProcesses, Connection con )
		throws Exception
    {
		final String descriptionDelimiter = "\n----------\n";
		
		ContractParamDAO billingParamDao = new ContractParamDAO( BILLING_USER, dbInfo );
		ProcessDAO processDao = new ProcessDAO( con );
		
		ResultSet rs = pd.executeQuery();
    	while( rs.next() )
    	{
    		int contractId = rs.getInt( 1 );
    		String contractTitle = rs.getString( 2 );
    		//long groups = rs.getLong( 3 );
    		BigDecimal balance = rs.getBigDecimal( 4 );
    		
    		if( log.isDebugEnabled() )
    		{
    			log.debug( "Contract: " + contractId + ", balance: " + balance );
    		}
    		
    		String description = "";
			description = "ФИО: " + billingParamDao.getTextParam( contractId, PARAM_BILLING_FIO ) + "\n";
			//description += "Телефон: " + ParamPhoneValueItem.toString( billingParamDao.getPhoneParam( contractId, PARAM_BILLING_PHONE ) ) + "\n";
			description += "Телефон: " + billingParamDao.getTextParam( contractId, PARAM_BILLING_PHONE ) + "\n";
			description += "Остаток: " + balance.toPlainString();

    		Process process = contractDebtProcesses.remove( contractId );
    		if( process == null )
    		{
    			log.info( "Creating process: " + description );

    			process = new Process();
    			process.setTypeId( PROCESS_TYPE_DEBTOR );
    			process.setDescription( description + descriptionDelimiter );

    			ProcessAction.processCreate( new DynActionForm( BILLING_USER ), con, process );

    			CommonObjectLink link = new CommonObjectLink( Process.OBJECT_TYPE, process.getId(), Contract.OBJECT_TYPE + ":" + dbInfo.getId(), 
    			                                              contractId, contractTitle );

    			LinkAction.addLink( new DynActionForm( BILLING_USER ), con, link );
    		}
    		// обновление описания
    		else
    		{
    			log.info( "Update process description: " + process.getId() );
    			
    			int pos = process.getDescription().indexOf( descriptionDelimiter );
    			if( pos > 0 )
    			{
    				process.setDescription( description + 
    				                        process.getDescription().substring( pos ) );    				
    			}
    			else
    			{
    				process.setDescription( description + 
    				                        descriptionDelimiter );
    			}
    			processDao.updateProcess( process );
    		}
    		
    		con.commit();
    	}
    	
    	pd.close();
    }
	

	@Override
	public void notify( UserEvent e, ConnectionSet connectionSet )
		throws BGException
	{
		/*if( e instanceof RunClassRequestEvent )
		{
			RunClassRequestEvent event = (RunClassRequestEvent)e;
			if( "payment".equals( event.getForm().getParam( "command" ) ) )
			{
				int contractId = event.getForm().getParamInt( "contractId", 0 );
				
				dbInfo = Utils.getFirst( DBInfoManager.getInstance().getDbInfoList() );
				if( dbInfo == null )
				{
					throw new BGException( "DBInfo not found" );
				}
				
				// проверка баланса
				ContractInfo info = new ContractDAO( BILLING_USER, dbInfo ).getContractInfo( contractId );
				//BigDecimal balance = info.getBalanceOut();				
			}
		}
		else */
		if( e instanceof ProcessChangedEvent )
		{
			ProcessChangedEvent event = (ProcessChangedEvent)e;
			
			Connection con = connectionSet.getConnection();
			
			int typeId = event.getProcess().getTypeId();
			if( typeId == PROCESS_TYPE_DEBTOR )
			{
    			// отключение
    			if( event.isStatus() && event.getProcess().getStatusId() == PROCESS_STATUS_DISCONNECT )
    			{
    				int processId = event.getProcess().getId();
    				
    				ProcessDAO processDao = new ProcessDAO( con );
    				SearchResult<Pair<String, Process>> searchResult = new SearchResult<Pair<String, Process>>();
    				processDao.searchLinkProcessList( searchResult, processId );
    				
    				for( Pair<String, Process> pair : searchResult.getList() )
    				{
    					if( pair.getSecond().getTypeId() == PROCESS_TYPE_DISCONNECT &&
    						pair.getSecond().getCloseTime() == null )
    					{
    						return;
    					}
    				}
    				
    				// создание процесса "Отключение"
    				Process process = new Process();
    				process.setTypeId( PROCESS_TYPE_DISCONNECT );
    				process.setDescription( event.getProcess().getDescription() );
    				
    				ProcessAction.processCreate( new DynActionForm( BILLING_USER ), con, process );
    				
    				new ProcessLinkDAO( con ).addLink( new CommonObjectLink( processId, Process.LINK_TYPE_MADE, process.getId(), "" ) );
    				new ParamValueDAO( con ).copyParams( processId, process.getId(), Arrays.asList( new Integer[]{ PROCESS_PARAM_ADDRESS } ) );
    				new ProcessLinkDAO( con ).copyLinks( processId, process.getId(), Contract.OBJECT_TYPE );
    				
    				//event.getForm().getResponse().addEvent( new ProcessChange )
    			}
			}
			else if( typeId == PROCESS_TYPE_DISCONNECT )
			{
				// отключение - закрытие процесса "Должник"
    			if( event.isStatus() && event.getProcess().getStatusId() == PROCESS_STATUS_CLOSE )
    			{
    				SearchResult<Pair<String, Process>> searchResult = new SearchResult<Pair<String, Process>>();
    				new ProcessDAO( con ).searchLinkedProcessList( searchResult, Process.LINK_TYPE_MADE, event.getProcess().getId(), 
    				                                               Collections.singleton( PROCESS_TYPE_DEBTOR ), null, null, null );
    				
    				for( Pair<String, Process> pair : searchResult.getList() )
    				{
    					Process process = pair.getSecond();
    					if( process.getCloseTime() != null )
    					{
    						continue;
    					}
    					
    					log.info( "Closing debt process: " + process.getId() );
    					
    					StatusChange change = new StatusChange();
        				change.setProcessId( process.getId() );
        				change.setStatusId( PROCESS_STATUS_CLOSE );
        				change.setDate( new Date() );
        				change.setUserId( User.USER_SYSTEM_ID );
        				change.setComment( "Отключен" );
        				
        				ProcessAction.processStatusUpdate( DynActionForm.SERVER_FORM, con, process, change );
    				}
    			}
			}
		}
	}
}