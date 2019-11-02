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
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObject;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;

public class ContractObjectDAO
    extends BillingDAO
{
	private static final String CONTRACT_OBJECT_MODULE_ID = "contract.object";

	public ContractObjectDAO( User user, String billingId )
	    throws BGException
	{
		super( user, billingId );
	}

	public ContractObjectDAO( User user, DBInfo dbInfo )
	    throws BGException
	{
		super( user, dbInfo );
	}

	public ContractObject getContractObject( int objectId )
	    throws BGException
	{
		Request request = new Request();
		request.setModule( CONTRACT_OBJECT_MODULE_ID );
		request.setAction( "ObjectGet" );
		request.setAttribute( "id", objectId );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "object" );

		ContractObject object = new ContractObject();
		if( nodeList.getLength() > 0 )
		{
			Element rowElement = (Element)nodeList.item( 0 );

			object.setId( objectId );

			object.setTitle( rowElement.getAttribute( "title" ) );
			object.setTypeId( Utils.parseInt( rowElement.getAttribute( "type_id" ) ) );

			try
			{
				SimpleDateFormat dateFormatter = new SimpleDateFormat( TimeUtils.PATTERN_DDMMYYYY );

				String dateFrom = rowElement.getAttribute( "date1" );
				if( Utils.notBlankString( dateFrom ) ) object.setDateFrom( dateFormatter.parse( dateFrom ) );

				String dateTo = rowElement.getAttribute( "date2" );
				if( Utils.notBlankString( dateTo ) ) object.setDateTo( dateFormatter.parse( dateTo ) );
			}
			catch( ParseException e )
			{
				throw new BGException( e );
			}
		}

		return object;
	}

	public void deleteContractObject( int contractId, int objectId )
	    throws BGException
	{
		Request request = new Request();
		request.setModule( CONTRACT_OBJECT_MODULE_ID );
		request.setAction( "ObjectDelete" );
		request.setAttribute( "id", objectId );
		request.setContractId( contractId );

		transferData.postData( request, user );
	}

	public void updateContractObject( ContractObject object )
	    throws BGException
	{
		updateContractObject( object.getId(),
		                      object.getTitle(),
		                      object.getDateFrom(),
		                      object.getDateTo(),
		                      object.getTypeId(),
		                      0 );
	}

	public void createContractObject( ContractObject object, int contractId )
	    throws BGException
	{
		object.setId( updateContractObject( object.getId(),
		                                    object.getTitle(),
		                                    object.getDateFrom(),
		                                    object.getDateTo(),
		                                    object.getTypeId(),
		                                    contractId ) );
	}

	public int updateContractObject( int objectId, String title, Date dateFrom, Date dateTo, int typeId, int contractId )
	    throws BGException
	{
		SimpleDateFormat dateFormatter = new SimpleDateFormat( TimeUtils.PATTERN_DDMMYYYY );
		Request request = new Request();
		request.setModule( CONTRACT_OBJECT_MODULE_ID );
		request.setAction( "ObjectUpdate" );
		request.setAttribute( "id", objectId );
		request.setAttribute( "title", Utils.maskNull( title ) );
		request.setAttribute( "type", typeId );

		if( dateFrom != null )
		{
			request.setAttribute( "date1", dateFormatter.format( dateFrom ) );
		}

		if( dateTo != null )
		{
			request.setAttribute( "date2", dateFormatter.format( dateTo ) );
		}

		if( contractId > 0 )
		{
			request.setAttribute( "cid", contractId );
		}

        return Utils.parseInt(XMLUtils.getElement(transferData.postData(request, user), "data").getAttribute("id"));
	}

	public List<ContractObject> getContractObjects( int contractId )
	    throws BGException
	{
		List<ContractObject> objects = new ArrayList<ContractObject>();

		Request request = new Request();
		request.setModule( CONTRACT_OBJECT_MODULE_ID );
		request.setAction( "ObjectTable" );
		request.setContractId( contractId );

		Document doc = transferData.postData( request, user );

		for( Element e : XMLUtils.selectElements( doc, "/data/table/data/row" ) )
		{
			ContractObject object = new ContractObject();
			object.setId( Utils.parseInt( e.getAttribute( "id" ) ) );
			object.setTitle( Utils.maskNull( e.getAttribute( "title" ) ) );
			object.setTypeId( Utils.parseInt( e.getAttribute( "type_id" ) ) );
			object.setType( Utils.maskNull( e.getAttribute( "type" ) ) );
			object.setPeriod( Utils.maskNull( e.getAttribute( "period" ) ) );

			/* падало на незакрытом периоде
			if( Utils.notEmptyString( object.getPeriod() ) )
			{
				try
				{
					SimpleDateFormat dateFormatter = new SimpleDateFormat( TimeUtils.PATTERN_DDMMYYYY );
					String[] parts = object.getPeriod().split( "-" );
					if( parts.length > 0 && Utils.notEmptyString( parts[0] ) ) object.setDateFrom( dateFormatter.parse( parts[0] ) );
					if( parts.length > 1 && Utils.notEmptyString( parts[1] ) ) object.setDateTo( dateFormatter.parse( parts[1] ) );
				}
				catch( ParseException ex )
				{
					throw new BGException( ex );
				}
			}*/

			objects.add( object );
		}

		return objects;
	}
}
