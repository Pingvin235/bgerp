package ru.bgcrm.plugin.bgbilling.docgen;

/*
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ru.bgcrm.dao.CustomerLinkDAO;
import ru.bgcrm.event.Event;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO;
import ru.bgcrm.plugin.bgbilling.model.CommonContract;
import ru.bgcrm.plugin.bgbilling.proto.model.Contract;
import ru.bgcrm.plugin.document.docgen.CommonDocumentGenerator;
import ru.bgcrm.plugin.document.docgen.CustomerDocumentGenerator;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

**
 * По мере перехода везде на XSLT 2.0 генерацию бланков - удалить.
 *
@Deprecated
public class CommonContractDocumentGenerator
    extends CommonDocumentGenerator
{
	private CommonContract commonContract;
	private CustomerDocumentGenerator customerGen;
	// генераторы для договоров по суффиксам
	private Map<String, ContractDocumentGenerator> contractGenMap = new HashMap<String, ContractDocumentGenerator>();

	public CommonContractDocumentGenerator()
	{
		super();
	}

	public CommonContractDocumentGenerator( Event e, ConnectionSet conSet )
	{
		super( e, conSet );
	}

	private CommonContract getCommonContract()
	    throws BGException
	{
		if( commonContract == null )
		{
			commonContract = new CommonContractDAO( conSet.getConnection() ).getContractById( event.getObjectId() );
		}
		return commonContract;
	}

	private ContractDocumentGenerator getContractGenerator( String suffix )
	    throws BGException
	{
		ContractDocumentGenerator result = contractGenMap.get( suffix );
		if( result != null )
		{
			return result;
		}

		CommonContract commonContract = getCommonContract();

		String title = commonContract.getFormatedNumber();
		int customerId = commonContract.getCustomerId();

		CustomerLinkDAO linkDao = new CustomerLinkDAO( conSet.getConnection() );
		for( CommonObjectLink link : linkDao.getObjectLinksWithType( customerId, Contract.OBJECT_TYPE + "%" ) )
		{
			String contractTitle = link.getLinkedObjectTitle();
			if( contractTitle.startsWith( title ) && contractTitle.endsWith( suffix ) )
			{
				DocumentGenerateEvent event = new DocumentGenerateEvent( this.event.getForm(), this.event.getPattern(),
				                                                         link.getLinkedObjectType(), link.getLinkedObjectId() );
				result = new ContractDocumentGenerator( event, conSet );
				contractGenMap.put( suffix, result );

				return result;
			}
		}

		return null;
	}

	private CustomerDocumentGenerator getCustomerGenerator()
	    throws BGException
	{
		if( customerGen == null )
		{
			int customerId = 0;

			CommonContract commonContract = getCommonContract();
			if( commonContract != null )
			{
				customerId = commonContract.getCustomerId();
			}

			DocumentGenerateEvent event = new DocumentGenerateEvent( this.event.getForm(), this.event.getPattern(), "customer", customerId );
			customerGen = new CustomerDocumentGenerator( event, conSet );
		}

		return customerGen;
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
		else if( macros.startsWith( "contractWithSuffix:" ) )
		{
			String[] tokens = macros.split( ":" );
			if( tokens.length < 2 )
			{
				throw new BGException( "Error macros contractWithSuffix: " + macros );
			}

			String suffix = tokens[1];

			ContractDocumentGenerator contractGen = getContractGenerator( suffix );
			if( contractGen != null )
			{
				String macrosEnd = Utils.substringAfter( macros, ":", 2 );
				return contractGen.processMacros( incomingValue, macrosEnd );
			}

			return "";
		}
		else if( macros.equals( "title" ) )
		{
			return getCommonContract().getFormatedNumber();
		}
		else if( macros.equals( "pswd" ) )
		{
			return getCommonContract().getPassword();
		}			
		else if( macros.startsWith( "contract" ) )
		{
			return super.processMacros( incomingValue, StringUtils.substringAfter( macros, ":" ) );
		}
		else if( macros.equals( "address" ) )
		{
			return getCommonContract().getAddress().getValue();
		}
		else if( macros.equals( "dateFrom" ) )
		{
			return String.valueOf( getCommonContract().getDateFrom().getTime() );
		}
		else
		{
			return super.processMacros( incomingValue, macros );
		}
	}
}
*/