package ru.bgcrm.plugin.bgbilling.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bgerp.app.exception.BGException;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.DBInfoManager;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.RequestToBilling;

/**
 * DAO в основном для вызова из Web - возвращает результат единообразно в виде
 * XML документа, в т.ч. ошибки связи и т.п.
 */
public class BGBillingDAO
    extends CommonDAO
{
	private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor( 0, 50, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>() );

	public BGBillingDAO()
	{
		super( null );
	}

	public Document doRequestToBilling( String dbKey, User user, Request request )
	    throws BGException
	{
		Document result = null;

		DBInfoManager dBinfoManager = DBInfoManager.getInstance();

		DBInfo dBInfo = dBinfoManager.getDbInfoMap().get( dbKey );
		if( dBInfo != null )
		{
			result = dBInfo.getTransferData().postData( request, user );
		}
		else
		{
			result = createDocWithError( "Некорректый идентификатор биллинга." );
		}

		return result;
	}

	private Document createDocWithError(String error) {
        Document doc = XMLUtils.newDocument();
        Element rootNode = XMLUtils.newElement(doc, "data");
        rootNode.setAttribute("status", "error");
        XMLUtils.createTextNode(rootNode, error);
        return doc;
    }

	public Map<String, Document> doRequestToBilling( Collection<String> dBkeys, User user, Request req )
	{
		Map<String, Document> result = new HashMap<>(dBkeys.size());

		DBInfoManager dBinfoManager = DBInfoManager.getInstance();

		AtomicInteger taskCount = new AtomicInteger();
		try
		{
			for( String dBkey : dBkeys )
			{
				DBInfo dBInfo = dBinfoManager.getDbInfoMap().get( dBkey );
				if( dBInfo != null )
				{
					RequestToBilling requestToBilling = new RequestToBilling( taskCount, dBInfo.getTransferData(), dBkey, user, req, result );
					taskCount.incrementAndGet();
					threadPool.execute( requestToBilling );
				}
			}

			while( taskCount.get() > 0 )
			{
				Thread.sleep( 300 );
			}
		}
		catch( Exception e )
		{
			log.error( e.getMessage(), e );
		}

		return result;
	}

	/*	private Request getRequestForVersion( DBInfo dBInfo, Request... request )
	    {
		    Request req = null;
		    for( Request r : request )
		    {
		    	if( dBInfo.getVersion().equals( r.getVersion() ) ||
		    		r.getVersion() == null )
		    	{
		    		req = r;
		    		break;
		    	}
		    }
		    return req;
	    }*/

	/*
	public void loadContractListFromXML( SearchResult<Contract> searchResult, Document billingDocument, String dBkey )
	{
	    NodeList contractsList = billingDocument.getElementsByTagName( "contracts" );
	    if ( contractsList != null && contractsList.getLength() > 0 && searchResult != null )
	    {
	        Page page = searchResult.getPage();
	        List<Contract> list = searchResult.getList();
	        Element contractsElement = (Element)contractsList.item( 0 );
	        NodeList itemList = contractsElement.getElementsByTagName( "item" );
	        if ( itemList != null && itemList.getLength() > 0 )
	        {
	            for ( int index = 0; index < itemList.getLength(); index++ )
	            {
	                Contract contract = new Contract();
	                Element itemElement = (Element)itemList.item( index );
	                contract.setId( Integer.parseInt( itemElement.getAttribute( "id" ) ) );
	                contract.setBillingId( dBkey );
	                contract.setTitle( itemElement.getAttribute( "title" ) );
	                contract.setComment( itemElement.getAttribute( "comment" ) );
	                String balance = itemElement.getAttribute( "balance" );
	                if ( balance != null && itemElement.getAttribute( "balance" ).length() > 0 )
	                {
	                    contract.setBalanceOut( new BigDecimal( balance ) );
	                }
	                list.add( contract );
	            }
	        }
	        page.setPageIndex( Utils.parseIntString( contractsElement.getAttribute( "pageIndex" ), 1 ) );
	        page.setPageSize( Utils.parseIntString( contractsElement.getAttribute( "pageSize" ), 25 ) );
	        page.setRecordCount( Utils.parseIntString( contractsElement.getAttribute( "recordCount" ), itemList.getLength() ) );
	    }
	}

	public void loadAllContractListFromXML( SearchResult<Contract> searchResult, Document billingDocument, String dBkey )
	{
	    NodeList contractsList = billingDocument.getElementsByTagName( "contracts" );
	    if ( contractsList != null && contractsList.getLength() > 0 && searchResult != null )
	    {
	        List<Contract> list = searchResult.getList();
	        Element contractsElement = (Element)contractsList.item( 0 );
	        NodeList itemList = contractsElement.getElementsByTagName( "item" );
	        if ( itemList != null && itemList.getLength() > 0 )
	        {
	            for ( int index = 0; index < itemList.getLength(); index++ )
	            {
	                Contract contract = new Contract();
	                Element itemElement = (Element)itemList.item( index );
	                contract.setId( Integer.parseInt( itemElement.getAttribute( "id" ) ) );
	                contract.setBillingId( dBkey );
	                contract.setTitle( itemElement.getAttribute( "title" ) );
	                contract.setComment( itemElement.getAttribute( "comment" ) );
	                String balance = itemElement.getAttribute( "balance" );
	                if ( balance != null && itemElement.getAttribute( "balance" ).length() > 0 )
	                {
	                    contract.setBalanceOut( new BigDecimal( balance ) );
	                }
	                list.add( contract );
	            }
	        }
	    }
	}
	*/
}
