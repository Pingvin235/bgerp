package ru.bgcrm.plugin.bgbilling.docgen;

/*
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.event.Event;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.plugin.bgbilling.dao.CerberCryptDAO;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.dao.ContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.plugin.bgbilling.ws.bgbilling.modules.cerbercrypt.common.BGException_Exception;
import ru.bgcrm.plugin.bgbilling.ws.bgbilling.modules.cerbercrypt.common.UserCard;
import ru.bgcrm.plugin.bgbilling.ws.bgbilling.modules.cerbercrypt.common.UserCardCopy;
import ru.bgcrm.plugin.bgbilling.ws.bgbilling.modules.cerbercrypt.common.WSUserCard;
import ru.bgcrm.plugin.bgbilling.ws.bgbilling.modules.cerbercrypt.common.WSUserCardCopy;
import ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator;
import ru.bgcrm.plugin.document.docgen.CustomerDocumentGenerator;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgcrm.util.sql.ConnectionSet;

**
 * По мере перехода везде на XSLT 2.0 генерацию бланков - удалить.
 *
@Deprecated
public class ContractDocumentGenerator
    extends CommonDocumentGenerator
{
	private Document contractInfo;
	private Document contractParams;
	private Document contractCard;

	private List<IdTitle> currentTariffOptions;

	private UserCard userCard;

	private CommonContractDocumentGenerator commonGen;
	private CustomerDocumentGenerator customerGen;

	public ContractDocumentGenerator()
	{
		super();
	}

	public ContractDocumentGenerator( Event e, ConnectionSet conSet )
	{
		super( e, conSet );
	}

	private CommonContractDocumentGenerator getCommonContractGenerator()
	    throws BGException
	{
		if( commonGen == null )
		{
			int customerId = getCustomerId();

			String contractTitle = XMLUtils.selectText( getContractInfo(), "/data/contract/@title", "" );

			for( CommonContract commonContract : new CommonContractDAO( conSet.getConnection() ).getContractList( customerId ) )
			{
				if( commonContract.getFormatedNumber().startsWith( contractTitle ) )
				{
					DocumentGenerateEvent event = new DocumentGenerateEvent( this.event.getForm(), this.event.getPattern(), "bgbilling-commonContract", commonContract.getId() );
					commonGen = new CommonContractDocumentGenerator( event, conSet );

					break;
				}
			}
		}

		return commonGen;
	}

	private CustomerDocumentGenerator getCustomerGenerator()
	    throws BGException
	{
		if( customerGen == null )
		{
			int customerId = getCustomerId();

			DocumentGenerateEvent event = new DocumentGenerateEvent( this.event.getForm(), this.event.getPattern(), "customer", customerId );
			customerGen = new CustomerDocumentGenerator( event, conSet );
		}

		return customerGen;
	}

	private int getCustomerId()
	    throws BGException
	{
		CommonObjectLink link = new CommonObjectLink();
		link.setLinkedObjectType( event.getObjectType() );
		link.setLinkedObjectId( event.getObjectId() );

		SearchResult<Customer> customerSearch = new SearchResult<Customer>();
		new CustomerLinkDAO( conSet.getConnection() ).searchCustomerByLink( customerSearch, link );

		int customerId = 0;
		if( customerSearch.getList().size() > 0 )
		{
			customerId = customerSearch.getList().iterator().next().getId();
		}
		return customerId;
	}

	private Document getContractParams()
	    throws BGException
	{
		if( contractParams == null )
		{
			String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
			contractParams = new ContractDAO( event.getUser(), billingId ).getContractParamsDoc( event.getObjectId() );
		}
		return contractParams;
	}

	private Document getContractInfo()
	    throws BGException
	{
		if( contractInfo == null )
		{
			String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
			contractInfo = new ContractDAO( event.getUser(), billingId ).getContractInfoDoc( event.getObjectId() );
		}
		return contractInfo;
	}

	private Document getContractCard()
	    throws BGException
	{
		if( contractCard == null )
		{
			String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
			contractCard = new ContractDAO( event.getUser(), billingId ).getContractCardDoc( event.getObjectId() );
		}
		return contractCard;
	}

	private List<IdTitle> getCurrentTariffOptions()
	    throws BGException
	{
		if( currentTariffOptions == null )
		{
			String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
			currentTariffOptions = new ContractDAO( event.getUser(), billingId ).getCurrentTariffOptionList( event.getObjectId() );
		}
		return currentTariffOptions;
	}

	private CerberCryptDAO getCerberCryptDAO()
	    throws BGException
	{
		String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
		return new CerberCryptDAO( billingId, event.getUser() );
	}

	@Override
	public String processMacros( String incomingValue, String macros )
	    throws BGException
	{
		if( macros.startsWith( "customer:" ) )
		{
			String macrosEnd = StringUtils.substringAfter( macros, ":" );

			return getCustomerGenerator().processMacros( incomingValue, macrosEnd );
		}
		else if( macros.startsWith( "commonContract:" ) )
		{
			String macrosEnd = StringUtils.substringAfter( macros, ":" );

			return getCommonContractGenerator().processMacros( incomingValue, macrosEnd );
		}
		else if( macros.startsWith( "param:" ) )
		{
			Document contractParams = getContractParams();

			String paramId = StringUtils.substringAfter( macros, ":" );

			return XMLUtils.selectText( contractParams, "/data/parameters/parameter[@pid='" + paramId + "']/@value", "" );
		}
		else if( macros.startsWith( "listParamId:" ) )
		{
			String paramId = StringUtils.substringAfter( macros, ":" );
			String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
			return String.valueOf( new ContractDAO( event.getUser(), billingId ).getParameterListValue( event.getObjectId(), Utils.parseInt( paramId, -1 ) ).getId() );
		}
		else if( StringUtils.endsWith( macros, "module:voiceip:contractCardXml" ) )
		{
			String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
			ContractDAO contractDAO = new ContractDAO( event.getUser(), billingId );

			StringWriter sw = new StringWriter();
			XMLUtils.serialize( contractDAO.getVoiceIpContractInfo( event.getObjectId() ), sw );
			return sw.toString();
		}
		else if( macros.startsWith( "module:cerbercrypt:" ) )
		{
			String macrosEnd = StringUtils.substringAfter( macros, "module:cerbercrypt:" );

			try
			{
				int pos = Utils.parseInt( StringUtils.substringBefore( macrosEnd, ":" ), -1 );

				if( pos != -1 )
				{
					WSUserCard wsUserCard = getCerberCryptDAO().getUserCardWSDAO().getWSUserCard();
					List<UserCard> userCardList = wsUserCard.getUserCardList( event.getObjectId(), true );

					int currentPos = 0;
					for( UserCard uc : userCardList )
					{
						if( currentPos == pos && uc.getBasecardId() == -1 )
						{
							userCard = uc;
							break;
						}
						else userCard = null;

						if( uc.getBasecardId() == -1 ) currentPos++;
					}
				}
				else
				{
					return processMacros( incomingValue, macrosEnd );
				}
			}
			catch( BGException_Exception e )
			{
				throw new BGException( e );
			}

			return processMacros( incomingValue, StringUtils.substringAfter( macrosEnd, ":" ) );
		}
		else if( macros.equals( "hasActiveCards" ) )
		{
			try
			{
				CerberCryptDAO cerberCryptDAO = getCerberCryptDAO();
				WSUserCard wsUserCard = cerberCryptDAO.getUserCardWSDAO().getWSUserCard();
				List<UserCard> userCardList = wsUserCard.getUserCardList( event.getObjectId(), true );
				for( UserCard uc : userCardList )
				{
					if( uc.getDate2() == null )
					{
						return "1";
					}
				}
			}
			catch( BGException_Exception e )
			{
				throw new BGException( e );
			}
			return "0";
		}
		else if( macros.equals( "isMultiRoom" ) )
		{
			try
			{
				CerberCryptDAO cerberCryptDAO = getCerberCryptDAO();
				WSUserCard wsUserCard = cerberCryptDAO.getUserCardWSDAO().getWSUserCard();
				WSUserCardCopy wsUserCardCopy = cerberCryptDAO.getUserCardCopyDAO().getWSUserCardCopy();

				List<UserCard> userCardList = wsUserCard.getUserCardList( event.getObjectId(), true );

				for( UserCard uc : userCardList )
				{
					List<UserCardCopy> userCardCopyList = wsUserCardCopy.getUserCardCopyList( uc.getId() );

					if( userCardCopyList.size() > 0 )
					{
						return "1";
					}
				}
			}
			catch( BGException_Exception e )
			{
				throw new BGException( e );
			}
			return "0";
		}
		else if( macros.startsWith( "cardNumber" ) )
		{
			if( userCard != null && userCard.getBasecardId() == -1 )
			{
				String cardNumber = String.valueOf( userCard.getNumber() );
				userCard = null;

				return cardNumber;
			}
			return "-";
		}
		else if( StringUtils.endsWith( macros, "slaveCardNumber" ) )
		{
			try
			{
				if( userCard != null )
				{
					WSUserCard wsUserCard = getCerberCryptDAO().getUserCardWSDAO().getWSUserCard();
					List<UserCard> userCardList = wsUserCard.getUserCardList( event.getObjectId(), true );

					for( UserCard uc : userCardList )
					{
						if( userCard.getId() == uc.getBasecardId() )
						{
							return String.valueOf( uc.getNumber() );
						}
					}
				}
			}
			catch( BGException_Exception e )
			{
				throw new BGException( e );
			}
			return "-";
		}
		else if( StringUtils.endsWith( macros, "сardPackets" ) )
		{
			String billingId = StringUtils.substringAfter( event.getObjectType(), ":" );
			CerberCryptDAO cerberCryptDAO = new CerberCryptDAO( billingId, event.getUser() );

			String formatedCardNumber = String.format( "%06d", userCard.getNumber() );

			Iterable<Element> elements = XMLUtils.selectElements( cerberCryptDAO.getUserCardPacketList( userCard.getId(), event.getObjectId() ), "/data/table/data/row[@card='" + formatedCardNumber + "']" );

			String packets = "";

			for( Element e : elements )
			{
				packets += e.getAttribute( "packet" );
			}

			return packets;
		}
		else if( macros.equals( "currentTariffId" ) )
		{
			return XMLUtils.selectText( getContractInfo(), "/data/info/tariff/item/@id", "" );
		}
		else if( macros.equals( "currentTariffTitle" ) )
		{
			return XMLUtils.selectText( getContractInfo(), "/data/info/tariff/item/@title", "" );
		}
		else if( macros.equals( "currentTariffIds" ) )
		{
			Set<String> ids = new HashSet<String>();
			for( Element el : XMLUtils.selectElements( getContractInfo(), "/data/info/tariff/item" ) )
			{
				ids.add( el.getAttribute( "id" ) );
			}
			return Utils.toString( ids );
		}
		else if( macros.equals( "currentTariffOptionIds" ) )
		{
			return Utils.getObjectIds( getCurrentTariffOptions() );
		}
		else if( macros.equals( "contractCardXml" ) )
		{
			StringWriter sw = new StringWriter();
			XMLUtils.serialize( getContractCard(), sw );
			return sw.toString();
		}
		else if( macros.equals( "title" ) )
		{
			return XMLUtils.selectText( getContractInfo(), "/data/contract/@title", "" );
		}
		else
		{
			return super.processMacros( incomingValue, macros );
		}
	}
}
*/