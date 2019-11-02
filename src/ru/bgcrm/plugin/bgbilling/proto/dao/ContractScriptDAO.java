package ru.bgcrm.plugin.bgbilling.proto.dao;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.bgcrm.model.BGException;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.script.ContractScript;
import ru.bgcrm.plugin.bgbilling.proto.model.script.ContractScriptLogItem;
import ru.bgcrm.util.Utils;

public class ContractScriptDAO
	extends BillingDAO
{
	private static final String CONTRACT_MODULE_ID = "contract";
	
	public ContractScriptDAO( User user, DBInfo dbInfo )
    	throws BGException
    {
    	super( user, dbInfo );
    }
	
	public ContractScriptDAO( User user, String billingId )
		throws BGException
	{
		super( user, billingId );
	}

	public List<ContractScript> contractScriptList( int contractId )
    	throws BGException
    {
    	Request request = new Request();
    	request.setModule( CONTRACT_MODULE_ID );
    	request.setAction( "ContractScriptTable" );
    	request.setContractId( contractId );
    
    	Document document = transferData.postData( request, user );
    
    	Element dataElement = document.getDocumentElement();
    	NodeList nodeList = dataElement.getElementsByTagName( "row" );
    
    	List<ContractScript> scriptList = new ArrayList<ContractScript>();
    	for( int index = 0; index < nodeList.getLength(); index++ )
    	{
    		Element rowElement = (Element)nodeList.item( index );
    		ContractScript script = new ContractScript();
    		script.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
    		script.setTitle( rowElement.getAttribute( "script" ) );
    		script.setComment( rowElement.getAttribute( "comment" ) );
    		script.setPeriod( rowElement.getAttribute( "period" ) );
    
    		scriptList.add( script );
    	}
    
    	return scriptList;
    }
    
    public void contractScriptLogList( SearchResult<ContractScriptLogItem> result, int contractId, String dateFrom, String dateTo )
    	throws BGException
    {
    	int pageIndex = result.getPage().getPageIndex();
    	int pageSize = result.getPage().getPageSize();
    
    	Request request = new Request();
    	request.setModule( CONTRACT_MODULE_ID );
    	request.setAction( "ContractScriptLog" );
    	request.setContractId( contractId );
    	request.setAttribute( "pageSize", pageSize );
    	request.setAttribute( "pageIndex", pageIndex );
    
    	if( Utils.notBlankString( dateFrom ) )
    	{
    		request.setAttribute( "start", dateFrom );
    	}
    	if( Utils.notBlankString( dateTo ) )
    	{
    		request.setAttribute( "end", dateTo );
    	}
    
    	Document document = transferData.postData( request, user );
    
    	Element dataElement = document.getDocumentElement();
    	NodeList nodeList = dataElement.getElementsByTagName( "row" );
    	List<ContractScriptLogItem> logList = result.getList();
    
    	for( int index = 0; index < nodeList.getLength(); index++ )
    	{
    		Element rowElement = (Element)nodeList.item( index );
    		ContractScriptLogItem logItem = new ContractScriptLogItem();
    		logItem.setCid( Utils.parseInt( rowElement.getAttribute( "cid" ) ) );
    		logItem.setData( rowElement.getAttribute( "data" ) );
    		logItem.setTime( rowElement.getAttribute( "time" ) );
    		logItem.setTitle( rowElement.getAttribute( "title" ) );
    
    		logList.add( logItem );
    	}
    
    	NodeList table = dataElement.getElementsByTagName( "table" );
    	if( table.getLength() > 0 )
    	{
    		result.getPage().setRecordCount( Utils.parseInt( ((Element)table.item( 0 )).getAttribute( "recordCount" ) ) );
    		result.getPage().setPageCount( Utils.parseInt( ((Element)table.item( 0 )).getAttribute( "pageCount" ) ) );
    	}
    }
    
    public ContractScript getContractScript( int scriptId )
    	throws BGException
    {
    	Request request = new Request();
    	request.setModule( CONTRACT_MODULE_ID );
    	request.setAction( "GetContractScript" );
    	request.setAttribute( "id", scriptId );
    
    	Document document = transferData.postData( request, user );
    
    	Element dataElement = document.getDocumentElement();
    	NodeList nodeList = dataElement.getElementsByTagName( "contract_script" );
    
    	if( nodeList.getLength() > 0 )
    	{
    		ContractScript script = new ContractScript();
    		Element rowElement = (Element)nodeList.item( 0 );
    
    		script.setId( scriptId );
    		script.setComment( rowElement.getAttribute( "comment" ) );
    		script.setDateFrom( rowElement.getAttribute( "date1" ) );
    		script.setDateTo( rowElement.getAttribute( "date2" ) );
    		script.setTypeId( Utils.parseInt( rowElement.getAttribute( "script" ) ) );
    
    		return script;
    	}
    
    	return null;
    }
    
    
    
    public void updateContractScript( int contractId, int scriptId, int scriptTypeId, String comment, String dateFrom, String dateTo )
    	throws BGException
    {
    	Request request = new Request();
    	request.setModule( CONTRACT_MODULE_ID );
    	request.setAction( "UpdateContractScript" );
    	if( scriptId == 0 )
    	{
    		request.setAttribute( "id", "new" );
    	}
    	else
    	{
    		request.setAttribute( "id", scriptId );
    	}
    	request.setContractId( contractId );
    	request.setAttribute( "script", scriptTypeId );
    	request.setAttribute( "comment", comment );
    	request.setAttribute( "date1", dateFrom );
    	request.setAttribute( "date2", dateTo );
    
    	transferData.postData( request, user );
    }
    
    public void deleteContractScript( int scriptId )
    	throws BGException
    {
    	Request request = new Request();
    	request.setModule( CONTRACT_MODULE_ID );
    	request.setAction( "DeleteContractScript" );
    	request.setAttribute( "id", scriptId );
    
    	transferData.postData( request, user );
    }
}