package ru.bgcrm.plugin.bgbilling.proto.dao;

import org.bgerp.app.exception.BGException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class AddressQuarterDAO
	extends BillingDAO
{
	public static String QNAME_FILTER_OFF = "";

	public AddressQuarterDAO( User user, DBInfo dbInfo )
		throws BGException
	{
		super( user, dbInfo );
	}

	public AddressQuarterDAO( User user, String billingId )
		throws BGException
	{
		super( user, billingId );
	}


	/* http://192.168.164.3:8084/bgbilling/executer?module=admin&qname=001&pageSize=25&action=GetAddressQuarter&BGBillingSecret=MVy2vFbxq3m0QCQINMFJAcND&cityid=31&pageIndex=1&
	<data secret="790FD377EA41B6FC047A9C6937F4AB68" status="ok"><table allRecord="1" pageCount="1" pageIndex="1" pageSize="25" recordCount="1"><data><row f0="427" f1="001(Школа)" f2="г. Орск" f3="31" f4="Группа Школа" f5="26"/></data></table></data>
	*/
	/**
	 * Определяет группу решения биллинга для quarterId, если таковой находится в выборке по cityId и quarterName
	 * @param quarterId - id квартала
	 * @param cityId - id города, если null -- указанный квартал ищется во всех городах
	 * @param quarterName - название квартала, если null или пустая строка -- выборка кварталов для поиска без учета названия
	 * @return группа решения биллинга; 0 если квартал в выборке не найден
	 * @throws BGException
	 */
	public int getGruopByQuarter( int quarterId, Integer cityId, String quarterName )
		throws BGException
	{
		Request request = new Request();
		request.setModule( "admin" );
		request.setAction( "GetAddressQuarter" );
		request.setPageIndex( 1 );
		request.setPageSize( 9999 );
		request.setAttribute( "cityid", cityId == null ? -1 : cityId );
		request.setAttribute( "qname", quarterName == null ? QNAME_FILTER_OFF : quarterName );

		Document document = transferData.postData( request, user );

		for( Element e : XMLUtils.selectElements( document, "/data/table/data/row" ) )
		{
			int qId = Utils.parseInt( e.getAttribute( "f0" ) );
			if( qId == quarterId )
			{
				return Utils.parseInt( e.getAttribute( "f5" ) );
			}
		}
		return 0;
	}

}