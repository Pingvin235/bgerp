package ru.bgcrm.plugin.bgbilling.proto.dao;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.TransferData;
import ru.bgcrm.plugin.bgbilling.proto.model.BillingUser;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdminDAO
{
	private static final String BGSECURE_MODULE_ID = "admin.bgsecure";

	protected User user;
	protected DBInfo dbInfo;
	protected TransferData transferData;

	public AdminDAO( User user, String billingId )
	throws BGException
	{
		this.user = user;
		this.dbInfo = DBInfoManager.getInstance()
								   .getDbInfoMap()
								   .get( billingId );
		if( dbInfo == null )
		{
			throw new BGException( "Не найден биллинг: " + billingId );
		}
		init();
	}

	public AdminDAO( User user, DBInfo dbInfo )
	throws BGException
	{
		this.user = user;
		this.dbInfo = dbInfo;

		init();
	}

	private static String getMD5Hash( String plaintext )
	throws BGException
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance( "MD5" );
		}
		catch( NoSuchAlgorithmException e )
		{
			throw new BGException( "NoSuchAlgorithmException - MD5" );
		}

		md.reset();
		md.update( plaintext.getBytes() );
		byte[] digest = md.digest();
		BigInteger bigInt = new BigInteger( 1, digest );
		String hashtext = bigInt.toString( 16 );
		// Now we need to zero pad it if you actually want the full 32 chars.
		while( hashtext.length() < 32 )
		{
			hashtext = "0" + hashtext;
		}
		return hashtext;
	}

	private void init()
	throws BGException
	{
		try
		{
			this.transferData = dbInfo.getTransferData();
		}
		catch( Exception e )
		{
			throw new BGException( e );
		}
	}

	public void UpdateUser( BillingUser bgUser, String user_pswd )
	throws BGException
	{

		///http://192.168.169.8:8080/bgbilling/executer?
		// contractCid=0&
		// groups=6&
		// login=volkov_y&
		// descr=%E4%E5%E6%F3%F0%ED%FB%E9+%F1%E8%F1.%E0%E4%EC%E8%ED%E8%F1%F2%F0%E0%F2%EE%F0+%28%D3%F4%E0%29&
		// contractGroups=0&
		// contractPid=0&
		// name=%C2%EE%EB%EA%EE%E2+%DF%EA%EE%E2+%D1%E5%F0%E3%E5%E5%E2%E8%F7&
		// contractGroupsMode=0&
		// id=728
		// pids=
		// actions=10%2C19%2C34%2C43%2Cp19%2Cp23%2Cp24&
		// config=&
		// email=&
		// status=0&

		Request request = new Request();
		request.setModule( BGSECURE_MODULE_ID );
		request.setAction( "UpdateUser" );

		request.setAttribute( "id", bgUser.getId() );

		request.setAttribute( "user_pswd", getMD5Hash( user_pswd ).toUpperCase() );
		request.setAttribute( "status", 0 );
		request.setAttribute( "contractPid", 0 );
		request.setAttribute( "contractGroupsMode", 0 );
		request.setAttribute( "name", bgUser.getName() );
		request.setAttribute( "login", bgUser.getLogin() );
		request.setAttribute( "contractGroups", 0 );
		request.setAttribute( "descr", bgUser.getDescribe() );
		//request.setAttribute( "groups",  );

		request.setAttribute( "contractCid", "" );
		request.setAttribute( "email", "" );
		request.setAttribute( "pids", "" );
		request.setAttribute( "config", "" );
		request.setAttribute( "actions", "" );

		transferData.postData( request, user );
	}

	/*private static void getBillingUserList()
	throws BGException
	{
		HashMap<String, BillingUser> userList = new HashMap<String, BillingUser>();

		Request request = new Request();
		request.setModule( "admin.bgsecure" );
		request.setAction( "UserTable" );

		Document document = transferData.postData( request, user );
		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "row" );
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			BillingUser billingUser = new BillingUser();

			billingUser.setDescribe( rowElement.getAttribute( "desc" ) );
			billingUser.setEmail( rowElement.getAttribute( "email" ) );
			billingUser.setGroupsTitle( rowElement.getAttribute( "groups" ) );
			billingUser.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			billingUser.setLogin( rowElement.getAttribute( "login" ) );
			billingUser.setName( rowElement.getAttribute( "name" ) );
			billingUser.setStatusTitle( rowElement.getAttribute( "status" ) );
			billingUser.setTitle( rowElement.getAttribute( "title" ) );
			billingUser.setUserActionText( rowElement.getAttribute( "user_action" ) );

			userList.put( billingUser.getLogin(), billingUser );
		}
	}*/

	/*public BillingUser getUser()
	{
		///http://192.168.169.8:8080/bgbilling/executer?
		// BGBillingSecret=gD1HmV9QCbreqn8YZdOVDcbz&
		// module=admin.bgsecure&
		// action=GetUser&
		// id=728&


		//<data secret="C2E7FBC9300AF192235BE85114E09303" status="ok">'
		// <user
		// actions=""
		// contractCid="0"
		// contractGroups=""
		// contractGroupsMode="0"
		// contractPid="0"
		// contractTitle=""
		// descr="дежурный сис.администратор (Уфа)"
		// email=""
		// groups="6"
		// laf=""
		// login="volkov_y"
		// name="Волков Яков Сергеевич"
		// pids=""
		// status="0"/>

	}*/

}
