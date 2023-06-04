package ru.bgcrm.plugin.bgbilling.proto.dao;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.model.base.IdTitle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.bgbilling.proto.model.dialup.*;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.sql.SQLUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DialUpDAO
extends BillingModuleDAO
{
	public static final int FIND_MODE_LOGIN = 1;
	public static final int FIND_MODE_ALIAS = 2;
	private static final String CALL_MODULE_ID = "call";
	private static final String DIALUP_MODULE_ID = "dialup";

	public DialUpDAO( User user, String billingId, int moduleId )
	throws BGException
	{
		super( user, billingId, moduleId );
	}

	public DialUpDAO( User user, DBInfo dbInfo, int moduleId )
	throws BGException
	{
		super( user, dbInfo.getId(), moduleId );
	}

	public static String getLogin( Document doc, int moduleId, int contractId )
	{
		return XMLUtils.selectText( doc, "/data/module[@id=" + moduleId + "]/user_login_" + moduleId + "[@cid=" + contractId + "]/@login" );
	}

	public static String getPassword( Document doc, int moduleId, int contractId )
	{
		return XMLUtils.selectText( doc, "/data/module[@id=" + moduleId + "]/user_login_" + moduleId + "[@cid=" + contractId + "]/@pswd" );
	}

	private static final String toHexString( String value )
	{
		char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuilder buf = new StringBuilder();

		for( int i = 0; i < value.length(); i++ )
		{
			int ch = (int)value.charAt( i );
			buf.append( HEX[(ch & 0x0000F000) >> 12] );
			buf.append( HEX[(ch & 0x00000F00) >> 8] );
			buf.append( HEX[(ch & 0x000000F0) >> 4] );
			buf.append( HEX[(ch & 0x0000000F)] );
		}
		return buf.toString();
	}

	/**
	 *
	 * @param contractId
	 * @return
	 * @throws BGException
	 * @throws Exception
	 */
	public List<DialUpLogin> getLoginList( int contractId )
	throws BGException
	{
		List<DialUpLogin> loginList = new ArrayList<>();

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "ContractInfo" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setContractId( contractId );
		req.setAttribute( "object_id", 0 );

		Document document = transferData.postData( req, user );
		for( Element rowElement : XMLUtils.selectElements( document, "/data/table/data/row" ) )
		{
			DialUpLogin login = new DialUpLogin();

			login.setId( Utils.parseInt( rowElement.getAttribute( "f0" ) ) );
			login.setLogin( Utils.parseInt( rowElement.getAttribute( "f1" ) ) );
			login.setAlias( rowElement.getAttribute( "f2" ) );
			TimeUtils.parsePeriod( rowElement.getAttribute( "f3" ), login );
			login.setSession( Utils.parseInt( rowElement.getAttribute( "f4" ) ) );
			login.setStatusTitle( rowElement.getAttribute( "f5" ) );
			login.setComment( rowElement.getAttribute( "f6" ) );

			loginList.add( login );
		}

		return loginList;
	}

	public DialUpLogin getLogin( int loginId )
	throws BGException
	{
		DialUpLogin login = null;

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "GeneralLoginInfo" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setAttribute( "lid", loginId );

		Document document = transferData.postData( req, user );

		Element rowElement = XMLUtils.selectElement( document, "/data/login" );
		if( rowElement != null )
		{
			login = new DialUpLogin();

			login.setId( loginId );
			login.setLogin( Utils.parseInt( rowElement.getAttribute( "login" ) ) );
			login.setAlias( rowElement.getAttribute( "alias" ) );
			login.setDateFrom( TimeUtils.parse( rowElement.getAttribute( "date1" ), TimeUtils.FORMAT_TYPE_YMD ) );
			login.setDateTo( TimeUtils.parse( rowElement.getAttribute( "date2" ), TimeUtils.FORMAT_TYPE_YMD ) );
			login.setSession( Utils.parseInt( rowElement.getAttribute( "session" ) ) );
			login.setStatus( Utils.parseInt( rowElement.getAttribute( "status" ) ) );
			login.setComment( rowElement.getAttribute( "comment" ) );
		}

		return login;
	}

	public void updatePassword( int loginId )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "UpdatePassword" );
		req.setModuleID( moduleId );
		req.setAttribute( "set_pswd", "1" );
		req.setAttribute( "lid", loginId );

		transferData.postData( req, user );
	}

	public int updateLogin( int contractId, DialUpLogin login, String pswd, boolean pswdAuto )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "UpdateLoginInfo" );
		req.setModuleID( moduleId );
		req.setContractId( contractId );
		req.setAttribute( "lid", login.getId() );
		req.setAttribute( "alias", login.getAlias() );
		req.setAttribute( "object_id", login.getObjectId() );
		req.setAttribute( "access", login.getStatus() );
		req.setAttribute( "session", login.getSession() );
		req.setAttribute( "comment", login.getComment() );
		req.setAttribute( "date1", TimeUtils.format( login.getDateFrom(), TimeUtils.FORMAT_TYPE_YMD ) );
		req.setAttribute( "date2", TimeUtils.format( login.getDateTo(), TimeUtils.FORMAT_TYPE_YMD ) );

		if( Utils.notBlankString( pswd ) )
		{
			req.setAttribute( "login_pswd", pswd );
		}
		if( pswdAuto )
		{
			req.setAttribute( "set_pswd", "1" );
		}

		transferData.postData( req, user );

		return 0;
	}

	public String getLoginPassword( int contractId, int login )
	throws BGException
	{
		Document contractCard = ContractDAO.getInstance( this.user, this.dbInfo ).getContractCardDoc( contractId );
		return XMLUtils.selectText( contractCard, "/data/module[@id=" + moduleId + "]/user_login_" + moduleId + "[@login=" + login + "]/@pswd" );
	}

	public DialUpLoginRadiusInfo getLoginRadiusInfo( int loginId )
	throws BGException
	{
		DialUpLoginRadiusInfo result = null;

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "RadiusInfo" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setAttribute( "lid", loginId );

		Document doc = transferData.postData( req, user );

		Element realmOptions = XMLUtils.selectElement( doc, "/data/realmOptions" );
		if( realmOptions != null )
		{
			result = new DialUpLoginRadiusInfo();
			result.setRealmGroup( realmOptions.getAttribute( "freedom" ) );
			result.setAttributeMode( Utils.parseInt( realmOptions.getAttribute( "rp_mode" ) ) );

			for( Element setEl : XMLUtils.selectElements( doc, "/data/set_table/data/row" ) )
			{
				DialUpLoginAttrSet set = new DialUpLoginAttrSet();
				set.setRealm( setEl.getAttribute( "realm" ) );
				set.setId( Utils.parseInt( setEl.getAttribute( "setId" ) ) );
				set.setTitle( setEl.getAttribute( "setTitle" ) );
				result.addAttrSet( set );
			}

			for( Element attrEl : XMLUtils.selectElements( doc, "/data/table/data/row" ) )
			{
				DialUpLoginAttr attr = new DialUpLoginAttr();
				attr.setRealm( attrEl.getAttribute( "realm" ) );
				attr.setName( attrEl.getAttribute( "attribute" ) );
				attr.setValue( attrEl.getAttribute( "value" ) );
				result.addAttr( attr );
			}
		}

		return result;
	}

	public void updateLoginRadiusInfo( int loginId, DialUpLoginRadiusInfo radiusInfo )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "UpdateRadiusInfo" );
		req.setModuleID( moduleId );
		req.setAttribute( "lid", loginId );

		req.setAttribute( "realm_group", radiusInfo.getRealmGroup() );
		req.setAttribute( "rp_mode", radiusInfo.getAttributeMode() );

		StringBuilder atribut = new StringBuilder();
		StringBuilder value = new StringBuilder();
		StringBuilder realm = new StringBuilder();
		for( DialUpLoginAttr attr : radiusInfo.getAttrList() )
		{
			Utils.addCommaSeparated( atribut, toHexString( attr.getName() ) );
			Utils.addCommaSeparated( value, toHexString( attr.getValue() ) );
			Utils.addCommaSeparated( realm, toHexString( attr.getRealm() ) );
		}

		req.setAttribute( "atribut", atribut.toString() );
		req.setAttribute( "value", value.toString() );
		req.setAttribute( "realm", realm.toString() );

		StringBuilder setIds = new StringBuilder();
		StringBuilder setRealms = new StringBuilder();
		for( DialUpLoginAttrSet set : radiusInfo.getAttrSetList() )
		{
			Utils.addCommaSeparated( setIds, String.valueOf( set.getId() ) );
			Utils.addCommaSeparated( setRealms, toHexString( set.getRealm() ) );
		}

		req.setAttribute( "setIds", setIds.toString() );
		req.setAttribute( "setRealms", setRealms.toString() );

		transferData.postData( req, user );
	}

	public List<DialUpLoginIp> getLoginIPAddress( int loginId )
	throws BGException
	{
		List<DialUpLoginIp> result = new ArrayList<>();

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "IPAddress" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setAttribute( "lid", loginId );

		Document document = transferData.postData( req, user );

		for( Element element : XMLUtils.selectElements( document, "/data/address/data/row" ) )
		{
			DialUpLoginIp ip = new DialUpLoginIp();
			ip.setAddress( element.getAttribute( "ip" ) );
			TimeUtils.parsePeriod( element.getAttribute( "period" ), ip );
			ip.setRealm( element.getAttribute( "realm" ) );

			result.add( ip );
		}

		return result;
	}

	public void updateLoginIpAddress( int loginId, List<DialUpLoginIp> addrList )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "IPAddressUpdate" );
		req.setModuleID( moduleId );
		req.setAttribute( "lid", loginId );

		StringBuilder values = new StringBuilder( 10 );
		for( DialUpLoginIp addr : addrList )
		{
			Utils.addCommaSeparated( values, addr.getAddress() + ":" + addr.getRealm() + ":" + TimeUtils.formatPeriod( addr.getDateFrom(), addr.getDateTo() ) );
		}
		req.setAttribute( "values", values.toString() );

		transferData.postData( req, user );
	}

	public List<DialUpLoginPasswordLogItem> getLoginPasswordLog( int loginId )
	throws BGException
	{
		List<DialUpLoginPasswordLogItem> result = new ArrayList<>();

		Request req = new Request();
		req.setModule( CALL_MODULE_ID );
		req.setAction( "PasswordLog" );
		req.setModuleID( moduleId );
		req.setAttribute( "lid", loginId );

		Document document = transferData.postData( req, user );

		for( Element el : XMLUtils.selectElements( document, "/data/table/data/row" ) )
		{
			DialUpLoginPasswordLogItem item = new DialUpLoginPasswordLogItem();
			item.setTime( TimeUtils.parse( el.getAttribute( "date_time" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS ) );
			item.setUser( el.getAttribute( "user" ) );
			result.add( item );
		}

		return result;
	}

	public List<Map<String, String>> getLoginParameter( int loginId )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "GetLoginParameter" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setAttribute( "lid", loginId );

		Document document = transferData.postData( req, user );
		List<Map<String, String>> loginParameterList = new ArrayList<>();

		NodeList rowList = document.getElementsByTagName( "row" );
		for( int rowIndex = 0; rowIndex < rowList.getLength(); rowIndex++ )
		{
			Element element = (Element)rowList.item( rowIndex );

			Map<String, String> loginParameter = new HashMap<>();
			loginParameter.put( "listValue", element.getAttribute( "listValue" ) );
			loginParameter.put( "name", element.getAttribute( "name" ) );
			loginParameter.put( "title", element.getAttribute( "title" ) );
			loginParameter.put( "type", element.getAttribute( "type" ) );
			loginParameter.put( "value", element.getAttribute( "value" ) );

			loginParameterList.add( loginParameter );
		}

		return loginParameterList;
	}

	public List<DialUpPeriod> getCalculatePeriodList( int contractId )
	throws BGException
	{
		List<DialUpPeriod> periodList = new ArrayList<>();

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "Period" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setContractId( contractId );

		Document document = transferData.postData( req, user );

		for( Element rowElement : XMLUtils.selectElements( document, "/data/table/data/row" ) )
		{
			DialUpPeriod period = new DialUpPeriod();

			period.setContractId( Utils.parseInt( rowElement.getAttribute( "cid" ) ) );
			period.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			period.setPeriodFrom( rowElement.getAttribute( "start" ) );
			period.setStartDate( TimeUtils.parse( rowElement.getAttribute( "start" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS ) );
			period.setPeriodTo( rowElement.getAttribute( "end" ) );
			period.setEndDate( TimeUtils.parse( rowElement.getAttribute( "end" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS ) );

			periodList.add( period );
		}

		return periodList;
	}

	public DialUpPeriod getCalculatePeriod( int contractId, int id )
	throws BGException
	{
		DialUpPeriod result = null;

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "Period" );
		req.setModuleID( moduleId );
		req.setContractId( contractId );
		req.setAttribute( "id", id );

		Document document = transferData.postData( req, user );

		Element period = XMLUtils.selectElement( document, "/data/period" );
		if( period != null )
		{
			result = new DialUpPeriod();
			result.setId( id );
			result.setContractId( Utils.parseInt( period.getAttribute( "cid" ) ) );
			result.setStartDate( TimeUtils.parse( period.getAttribute( "start" ), TimeUtils.PATTERN_DDMMYYYY ) );
			result.setEndDate( TimeUtils.parse( period.getAttribute( "end" ), TimeUtils.PATTERN_DDMMYYYY ) );
		}

		return result;
	}

	public void updateCaclulatePeriod( DialUpPeriod period )
	throws BGException
	{
		updateCaclulatePeriod( period.getContractId(), period.getId(), period.getStartDate(), period.getEndDate() );
	}

	public void updateCaclulatePeriod( int contractId, int id, Date fromDate, Date toDate )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setAction( "PeriodUpdate" );
		req.setAttribute( "cid", contractId );
		req.setAttribute( "start", TimeUtils.format( fromDate, TimeUtils.FORMAT_TYPE_YMD ) );
		req.setAttribute( "end", TimeUtils.format( toDate, TimeUtils.FORMAT_TYPE_YMD ) );
		if( id > 0 )
		{
			req.setAttribute( "id", id );
		}
		else
		{
			req.setAttribute( "id", 0 );
		}

		transferData.postData( req, user );
	}

	/**
	 *
	 * @param contractId
	 * @param loginId
	 * @param days
	 * @return
	 * @throws Exception
	 */
	public List<DialUpSession> getSessionList( int contractId, int loginId, int days )
	throws BGException
	{
		Calendar dateTo = new GregorianCalendar();
		Calendar dateFrom = new GregorianCalendar();
		dateFrom.add( Calendar.DAY_OF_YEAR, -days );

		List<DialUpSession> sessionList = new ArrayList<>();

		Connection dbConnection = dbInfo.getConnectionPool().getDBSlaveConnectionFromPool();
		if( dbConnection != null )
		{
			try
			{
				Contract contract = ContractDAO.getInstance( user, dbInfo ).getContractById( contractId );

				SimpleDateFormat getModuleMonthTableNameFormat = new SimpleDateFormat( "_" + moduleId + "_yyyyMM" );

				StringBuilder query = new StringBuilder();
				Calendar date = new GregorianCalendar();
				while( dateFrom.get( Calendar.YEAR ) <= date.get( Calendar.YEAR ) &&
					   dateFrom.get( Calendar.MONTH ) <= date.get( Calendar.MONTH ) )
				{
					StringBuilder tableName = new StringBuilder( "log_session" );
					tableName.append( getModuleMonthTableNameFormat.format( date.getTime() ) );

					query.append( " SELECT * FROM " )
						 .append( tableName )
						 .append( " AS " + tableName )
						 .append( " WHERE " )
						 .append( tableName + ".lid=" + loginId + " AND " )
						 .append( tableName + ".session_stop BETWEEN '" + TimeUtils.format( dateFrom.getTime(), TimeUtils.PATTERN_YYYYMMDD ) + "'"
								  + " AND '" + TimeUtils.format( dateTo.getTime(), TimeUtils.PATTERN_YYYYMMDD ) + "'" );

					date.add( Calendar.MONTH, -1 );
					if( dateFrom.get( Calendar.YEAR ) <= date.get( Calendar.YEAR ) &&
						dateFrom.get( Calendar.MONTH ) <= date.get( Calendar.MONTH ) )
					{
						query.append( " UNION " );
					}
				}

				query.append( " ORDER BY session_start DESC " );

				PreparedStatement ps = dbConnection.prepareStatement( query.toString() );
				ResultSet rs = ps.executeQuery();

				while( rs.next() )
				{
					DialUpSession session = new DialUpSession();

					session.setId( rs.getInt( "id" ) );
					session.setFromNumber( rs.getString( "from_number" ) );
					session.setToNumber( rs.getString( "to_number" ) );
					session.setBytesIn( rs.getLong( "input_octets" ) );
					session.setBytesIn( rs.getLong( "output_octets" ) );

					long ip = rs.getLong( "ipaddr" );
					session.setIpAddress( InetAddress.getByAddress( new byte[] { (byte)(ip >> 24), (byte)(ip >> 16), (byte)(ip >> 8), (byte)ip } ) );
					session.setSessionCost( rs.getDouble( "session_cost" ) );
					session.setSessionStart( rs.getTimestamp( "session_start" ) );
					session.setSessionStop( rs.getTimestamp( "session_stop" ) );
					session.setSessionTimeInMillis( rs.getInt( "session_time" ) * 1000L );
					session.setActive( !rs.getBoolean( "status" ) );
					session.setContract( contract.getTitle() );
					session.setCid( contractId );
					session.setRadiusLogId( rs.getInt( "lr" ) );

					sessionList.add( session );
				}
			}
			catch( SQLException | UnknownHostException e )
			{
				throw new BGMessageException( e.getMessage() );
			}
			finally
			{
				SQLUtils.closeConnection( dbConnection );
			}
		}
		else
		{
			try
			{
				while( TimeUtils.dateBefore( dateFrom, dateTo ) )
				{
					Request req = new Request();
					req.setModule( DIALUP_MODULE_ID );
					req.setAction( "GetLogAndError" );
					req.setModuleID( String.valueOf( moduleId ) );
					req.setContractId( contractId );
					req.setAttribute( "lid", loginId );
					req.setAttribute( "mode", "logs" );
					req.setAttribute( "date", TimeUtils.format( dateTo.getTime(), "dd.MM.yyyy 00:00" ) );
					req.setPageSize( 999 );

					Document document = transferData.postData( req, user );

					Element dataElement = document.getDocumentElement();
					NodeList nodeList = dataElement.getElementsByTagName( "row" );

					for( int index = nodeList.getLength() - 1; index >= 0; index-- )
					{
						DialUpSession session = new DialUpSession();
						Element rowElement = (Element)nodeList.item( index );

						session.setId( Utils.parseInt( rowElement.getAttribute( "record_id" ) ) );
						session.setCid( Utils.parseInt( rowElement.getAttribute( "cid" ) ) );
						session.setContract( rowElement.getAttribute( "contract" ) );
						session.setFromNumber( StringUtils.substringBefore( rowElement.getAttribute( "from_to" ), "/" ).trim() );
						session.setToNumber( StringUtils.substringAfter( rowElement.getAttribute( "from_to" ), "/" ).trim() );
						session.setBytesIn( Utils.parseLong( rowElement.getAttribute( "in" ) ) );
						session.setBytesOut( Utils.parseLong( rowElement.getAttribute( "out" ) ) );
						session.setIpAddress( InetAddress.getByName( rowElement.getAttribute( "ipaddr" ) ) );
						session.setSessionCost( Double.parseDouble( rowElement.getAttribute( "session_cost" ) ) );
						session.setSessionStart( TimeUtils.parse( rowElement.getAttribute( "session_start" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS ) );
						session.setSessionStop( TimeUtils.parse( rowElement.getAttribute( "session_stop" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS ) );
						session.setSessionTimeInMillis( Utils.parseLong( StringUtils.substringBetween( rowElement.getAttribute( "session_time" ), "[", "]" ) ) * 1000L );
						session.setActive( Utils.parseBoolean( rowElement.getAttribute( "act" ) ) );
						session.setRadiusLogId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );

						sessionList.add( session );
					}

					dateTo.add( Calendar.DAY_OF_YEAR, -1 );
				}
			}
			catch( UnknownHostException e )
			{
				throw new BGMessageException( e.getMessage() );
			}

		}

		return sessionList;
	}

	public List<DialUpError> getErrorList( int contractId, int loginId, int days )
	throws BGException
	{
		Calendar currentDate = new GregorianCalendar();
		Calendar dateFrom = new GregorianCalendar();
		dateFrom.add( Calendar.DAY_OF_YEAR, -days );

		List<DialUpError> errorList = new ArrayList<>();

		while( TimeUtils.dateBefore( dateFrom, currentDate ) )
		{
			Request req = new Request();
			req.setModule( DIALUP_MODULE_ID );
			req.setAction( "GetLogAndError" );
			req.setModuleID( String.valueOf( moduleId ) );
			req.setContractId( contractId );
			req.setAttribute( "lid", loginId );
			req.setAttribute( "mode", "error" );
			req.setAttribute( "only_login", 1 );
			req.setAttribute( "date", TimeUtils.format( currentDate.getTime(), "dd.MM.yyyy 00:00" ) );
			req.setPageSize( 999 );

			Document document = transferData.postDataSync( req, user );

			Element dataElement = document.getDocumentElement();
			NodeList nodeList = dataElement.getElementsByTagName( "row" );

			for( int index = 0; index < nodeList.getLength(); index++ )
			{
				DialUpError error = new DialUpError();
				Element rowElement = (Element)nodeList.item( index );

				String contract = rowElement.getAttribute( "f3" );
				if( contract != null && !"-".equals( contract ) )
				{
					error.setId( rowElement.getAttribute( "f0" ) );
					error.setRecordId( rowElement.getAttribute( "f0" ) );
					error.setCid( rowElement.getAttribute( "f1" ) );
					error.setDate( rowElement.getAttribute( "f2" ) );
					error.setContract( rowElement.getAttribute( "f3" ) );
					error.setLogin( rowElement.getAttribute( "f5" ) );
					error.setNas( rowElement.getAttribute( "f6" ) );
					error.setError( rowElement.getAttribute( "f7" ) );
					errorList.add( error );
				}
			}

			currentDate.add( Calendar.DAY_OF_YEAR, -1 );
		}

		return errorList;
	}

	public String getRadiusLog( String radiusSessionId, String radiusSessionStart )
	throws Exception
	{
		return getRadiusLog( radiusSessionId, radiusSessionStart, "\n" );
	}

	public String getRadiusLog( String radiusSessionId, String radiusSessionStart, String splitter )
	throws BGException
	{
		String radiusLog = "";

		Request req = new Request();
		req.setModule( CALL_MODULE_ID );
		req.setAction( "GetRadiusLogInfo" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setAttribute( "id", radiusSessionId );
		req.setAttribute( "date", TimeUtils.format( TimeUtils.parse( radiusSessionStart, TimeUtils.PATTERN_DDMMYYYY ), "01.MM.yyyy" ) );

		Document document = transferData.postData( req, user );
		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "row" );

		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );

			Node firstChild = rowElement.getFirstChild();
			radiusLog += firstChild == null ? "" : firstChild.getNodeValue();
			radiusLog += splitter;
		}

		return radiusLog;
	}

	public void terminateSession( String recordId )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "SendRadiusCommand" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setAttribute( "command", "kill" );
		req.setAttribute( "record_id", recordId );

		transferData.postData( req, user );
	}

	public List<DialUpSession> getActiveSessionList( int contractId, int loginId )
	throws BGException
	{
		List<DialUpSession> activeSessionList = new ArrayList<>();

		try
		{
			Request req = new Request();
			req.setModule( DIALUP_MODULE_ID );
			req.setAction( "GetLogAndError" );
			req.setModuleID( String.valueOf( moduleId ) );
			req.setContractId( contractId );
			req.setAttribute( "lid", loginId );
			req.setAttribute( "mode", "active" );

			Document document = transferData.postData( req, user );

			Element dataElement = document.getDocumentElement();
			NodeList nodeList = dataElement.getElementsByTagName( "row" );

			for( int index = nodeList.getLength() - 1; index >= 0; index-- )
			{
				DialUpSession session = new DialUpSession();
				Element rowElement = (Element)nodeList.item( index );

				session.setId( Utils.parseInt( rowElement.getAttribute( "record_id" ) ) );
				session.setCid( Utils.parseInt( rowElement.getAttribute( "cid" ) ) );
				session.setContract( rowElement.getAttribute( "contract" ) );
				session.setFromNumber( StringUtils.substringBefore( rowElement.getAttribute( "from_to" ), "/" ).trim() );
				session.setToNumber( StringUtils.substringAfter( rowElement.getAttribute( "from_to" ), "/" ).trim() );
				session.setBytesIn( Utils.parseLong( rowElement.getAttribute( "in" ) ) );
				session.setBytesOut( Utils.parseLong( rowElement.getAttribute( "out" ) ) );
				session.setIpAddress( InetAddress.getByName( rowElement.getAttribute( "ipaddr" ) ) );
				session.setSessionCost( Double.parseDouble( rowElement.getAttribute( "session_cost" ) ) );
				session.setSessionStart( TimeUtils.parse( rowElement.getAttribute( "session_start" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS ) );
				session.setSessionStop( TimeUtils.parse( rowElement.getAttribute( "session_stop" ), TimeUtils.PATTERN_DDMMYYYYHHMMSS ) );
				session.setSessionTimeInMillis( Utils.parseLong( StringUtils.substringBetween( rowElement.getAttribute( "session_time" ), "[", "]" ) ) * 1000L );
				session.setActive( Utils.parseBoolean( rowElement.getAttribute( "act" ) ) );
				session.setRadiusLogId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );

				activeSessionList.add( session );
			}
		}
		catch( UnknownHostException e )
		{
			throw new BGMessageException( e.getMessage() );
		}

		return activeSessionList;
	}

	public List<DialUpLoginRestrict> getRestrictList( int loginId, int contractId )
	throws ParseException, BGException
	{
		List<DialUpLoginRestrict> restrictList = new ArrayList<>();

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "ListServicesLimit" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setContractId( contractId );
		req.setAttribute( "lid", loginId );

		Document document = transferData.postData( req, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "service" );

		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );

			DialUpLoginRestrict restrict = new DialUpLoginRestrict();

			restrict.setComment( rowElement.getAttribute( "comment" ) );
			restrict.setDateFrom( TimeUtils.parse( rowElement.getAttribute( "date1" ), TimeUtils.PATTERN_DDMMYYYY ) );
			restrict.setDateTo( TimeUtils.parse( rowElement.getAttribute( "date2" ), TimeUtils.PATTERN_DDMMYYYY ) );
			restrict.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			restrict.setServiceId( Utils.parseInt( rowElement.getAttribute( "sid" ) ) );
			restrict.setType( Utils.parseInt( rowElement.getAttribute( "type" ) ) );

			int accessParam = Utils.parseInt( rowElement.getAttribute( "param1" ) );
			if( accessParam == 0 )
			{
				restrict.setAccess( DialUpLoginRestrict.Access.ALLOW );
			}
			else if( accessParam == 1 )
			{
				restrict.setAccess( DialUpLoginRestrict.Access.DENY );
			}

			int restrictParam = Utils.parseInt( rowElement.getAttribute( "param2" ) );
			if( restrictParam == 0 )
			{
				restrict.setRestrict( DialUpLoginRestrict.Restrict.ENTER );
			}
			else if( restrictParam == 1 )
			{
				restrict.setRestrict( DialUpLoginRestrict.Restrict.WORK );
			}

			//DialUpLoginRestrict.RestrictTime restrictTime = new DialUpLoginRestrict().new RestrictTime();

			/*restrictTime.setActiveHours( activeHours );
			restrictTime.setActiveMinutes( activeMinutes );
			restrictTime.setDayOfMonth(  );
			restrictTime.setDayOfWeek( dayOfWeek );

			restrict.setRestrictTime( restrictTime );*/

			restrictList.add( restrict );
		}
		return restrictList;
	}

	public void updatePeriod( int contractId, Date dateFrom, Date dateTo )
	throws BGException
	{
		updatePeriod( contractId, dateFrom, dateTo, 0 );
	}

	public void updatePeriod( int contractId, Date dateFrom, Date dateTo, int periodId )
	throws BGException
	{
		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setAction( "PeriodUpdate" );
		req.setModuleID( moduleId );
		req.setContractId( contractId );
		if( periodId > 0 )
		{
			req.setAttribute( "id", periodId );
		}

		if( dateFrom != null )
		{
			req.setAttribute( "start", TimeUtils.format( dateFrom, TimeUtils.PATTERN_DDMMYYYY ) );
		}
		if( dateTo != null )
		{
			req.setAttribute( "end", TimeUtils.format( dateTo, TimeUtils.PATTERN_DDMMYYYY ) );
		}

		transferData.postData( req, user );
	}

	public List<IdStringTitle> getRealmList()
	throws BGException
	{
		List<IdStringTitle> result = new ArrayList<>();

		Request req = new Request();
		req.setModule( DIALUP_MODULE_ID );
		req.setModuleID( moduleId );
		req.setAction( "RealmList" );

		Document doc = transferData.postData( req, user );

		for( Element itemEl : XMLUtils.selectElements( doc, "/data/realms/item" ) )
		{
			IdStringTitle item = new IdStringTitle();
			item.setId( itemEl.getAttribute( "id" ) );
			item.setTitle( itemEl.getAttribute( "title" ) );
			result.add( item );
		}

		return result;
	}

	public List<IdTitle> getAttrSetList()
	throws BGException
	{
		List<IdTitle> result = new ArrayList<>();

		Request req = new Request();
		req.setModule( CALL_MODULE_ID );
		req.setModuleID( moduleId );
		req.setAction( "AttrSetList" );

		Document doc = transferData.postData( req, user );

		for( Element itemEl : XMLUtils.selectElements( doc, "/data/sets/item" ) )
		{
			result.add( new IdTitle( Utils.parseInt( itemEl.getAttribute( "id" ) ), itemEl.getAttribute( "title" ) ) );
		}

		return result;
	}

	public List<String> getAttrTypeList()
	throws BGException
	{
		List<String> result = new ArrayList<>();

		Request req = new Request();
		req.setModule( CALL_MODULE_ID );
		req.setModuleID( moduleId );
		req.setAction( "RadiusAttributeTypeList" );

		Document doc = transferData.postData( req, user );

		for( Element itemEl : XMLUtils.selectElements( doc, "/data/types/item" ) )
		{
			result.add( itemEl.getAttribute( "id" ) );
		}

		return result;
	}

	public List<IdStringTitle> getRealmGroupList()
	throws BGException
	{
		List<IdStringTitle> result = new ArrayList<>();

		Request req = new Request();
		req.setModule( CALL_MODULE_ID );
		req.setModuleID( moduleId );
		req.setAction( "RealmGroupList" );

		Document doc = transferData.postData( req, user );

		for( Element itemEl : XMLUtils.selectElements( doc, "/data/realmgrs/item" ) )
		{
			IdStringTitle item = new IdStringTitle();
			item.setId( itemEl.getAttribute( "id" ) );
			item.setTitle( itemEl.getAttribute( "title" ) );
			result.add( item );
		}

		return result;
	}

	public List<DialUpLogin> findLogin( String filter, int findMode )
	throws BGException
	{
		List<DialUpLogin> result = new ArrayList<DialUpLogin>();

		Request req = new Request();
		req.setModule( CALL_MODULE_ID );
		req.setModuleID( moduleId );
		req.setAction( "FindContractLoginAlias" );
		if( findMode == FIND_MODE_LOGIN )
		{
			req.setAttribute( "type", "login" );
		}
		else
		{
			req.setAttribute( "type", "alias" );
		}
		req.setAttribute( "value", filter );

		Document doc = transferData.postData( req, user );
		for( Element el : XMLUtils.selectElements( doc, "/data/table/data/row" ) )
		{
			//03BF25046BAD192465" status="ok"><table><data><row f0="5425" f1="C0613-04" f2="5404" f3="608" f4="www"/></data></table></data>
			DialUpLogin login = new DialUpLogin();
			login.setId( Utils.parseInt( el.getAttribute( "f2" ) ) );
			login.setContractId( Utils.parseInt( el.getAttribute( "f0" ) ) );
			login.setContractTitle( el.getAttribute( "f1" ) );
			login.setLogin( Utils.parseInt( el.getAttribute( "f3" ) ) );
			login.setAlias( el.getAttribute( "f4" ) );

			result.add( login );
		}

		return result;
	}
}
