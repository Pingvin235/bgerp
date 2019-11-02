package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.voiceip.VoiceIpLogin;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class VoiceIpDAO
	extends BillingDAO
{
	private static final String VOICEIP_MODULE_ID = "voiceip";
	private int moduleId;
	
	public VoiceIpDAO( User user, String billingId, int moduleId )
		throws BGException
	{
		super( user, billingId );
		this.moduleId = moduleId;
	}

	@Deprecated
	public VoiceIpDAO( User user, String billingId )
		throws BGException
	{
		super( user, billingId );
		DBInfo dbinfo = DBInfoManager.getDbInfo( billingId );
		moduleId = dbinfo.getSetup().getInt( "module.voiceip.id", -1 );
	}

	public List<VoiceIpLogin> getVoiceIpLogins( int contractId )
		throws BGMessageException
	{
		Request req = new Request();
		req.setModule( VOICEIP_MODULE_ID );
		req.setAction( "ContractInfo" );
		req.setModuleID( String.valueOf( moduleId ) );
		req.setContractId( contractId );
		req.setAttribute( "object_id", 0 );

		Document document = null;
		try
		{
			document = transferData.postData( req, user );
		}
		catch( Exception e )
		{}

		List<VoiceIpLogin> voiceIpLogins = new ArrayList<VoiceIpLogin>();
		if( document != null )
		{
			Element dataElement = document.getDocumentElement();
			NodeList nodeList = dataElement.getElementsByTagName( "row" );

			for( int index = 0; index < nodeList.getLength(); index++ )
			{
				VoiceIpLogin login = new VoiceIpLogin();
				Element element = (Element)nodeList.item( index );

				login.setId( Utils.parseInt( element.getAttribute( "f0" ) ) );
				login.setLogin( element.getAttribute( "f1" ) );
				login.setAlias( element.getAttribute( "f2" ) );
				login.setPeriod( element.getAttribute( "f3" ) );
				login.setType( element.getAttribute( "f4" ) );
				login.setAccess( element.getAttribute( "f5" ) );
				login.setComment( element.getAttribute( "f6" ) );

				voiceIpLogins.add( login );
			}
		}

		return voiceIpLogins;
	}

	public VoiceIpLogin getVoiceIpLogin( int loginId )
		throws BGException
	{
		VoiceIpLogin login = new VoiceIpLogin();
		Request request = new Request();
		request.setModule( VOICEIP_MODULE_ID );
		request.setAction( "GeneralLoginInfo" );
		request.setModuleID( moduleId );
		request.setAttribute( "lid", loginId );

		Document doc = transferData.postData( request, user );

		Element e = XMLUtils.selectElement( doc, "/data/login" );
		login.setId( loginId );
		login.setAlias( e.getAttribute( "alias" ) );
		login.setComment( e.getAttribute( "comment" ) );
		login.setLogin( e.getAttribute( "login" ) );
		login.setObjectId( Utils.parseInt( e.getAttribute( "object_id" ) ) );
		login.setType( e.getAttribute( "type" ) );

		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat( TimeUtils.PATTERN_DDMMYYYY );
			if( Utils.notEmptyString( e.getAttribute( "date1" ) ) ) login.setDateFrom( sdf.parse( e.getAttribute( "date1" ) ) );
			if( Utils.notEmptyString( e.getAttribute( "date2" ) ) ) login.setDateTo( sdf.parse( e.getAttribute( "date2" ) ) );
		}
		catch( ParseException ex )
		{
			throw new BGException( ex );
		}

		return login;
	}

	public void updateVoiceIpLogin( int contractId, VoiceIpLogin login )
		throws BGException
	{
		updateVoiceIpLogin( contractId,
							login.getId(),
							login.getAlias(),
							login.getObjectId(),
							login.getComment(),
							login.getDateFrom(),
							login.getDateTo(),
							login.getType(),
							login.getPassword(),
							Utils.isEmptyString( login.getPassword() ) );
	}

	public void updateVoiceIpLogin( int contractId, int loginId, String alias, int objectId, String comment, Date dateFrom, Date dateTo, String type, String password, boolean setPassword )
		throws BGException
	{
		Request req = new Request();

		req.setModule( VOICEIP_MODULE_ID );
		req.setModuleID( moduleId );
		req.setAction( "UpdateLoginInfo" );
		req.setContractId( contractId );
		req.setAttribute( "lid", loginId );
		req.setAttribute( "alias", alias );
		req.setAttribute( "object_id", objectId );
		req.setAttribute( "comment", comment );
		req.setAttribute( "type", type );

		SimpleDateFormat sdf = new SimpleDateFormat( TimeUtils.PATTERN_DDMMYYYY );
		if( dateFrom != null ) req.setAttribute( "date1", sdf.format( dateFrom ) );
		if( dateTo != null ) req.setAttribute( "date2", sdf.format( dateTo ) );

		if( setPassword )
		{
			req.setAttribute( "set_pswd", "1" );
		}
		else if( Utils.notBlankString( password ) )
		{
			req.setAttribute( "login_pswd", password );
		}

		transferData.postData( req, user );
	}
}
