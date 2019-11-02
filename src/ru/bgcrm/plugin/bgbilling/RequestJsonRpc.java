package ru.bgcrm.plugin.bgbilling;

import java.util.HashMap;
import java.util.Map;

/*
 Примерный URL запроса:
 
 http://127.0.0.1:8080/bgbilling/executer/json/ru.bitel.bgbilling.kernel.contract.api/ContractService
	{"method" : "contractList",
	"user" :{ "user" : "shamil", "pswd" : "xxxx" },
	"params" : {
	"title" : "0",
	"fc" : -1,
	"groupMask" : 0,
	"subContracts" : false,
	"closed" : true,
	"hidden" : false,
	"page" : { "pageIndex" : 2, "pageSize" : 2 }
	} }
	
 Примерный ответ:
 
 {"status":"ok","message":"",
    "data":
    {
     "page":{"pageSize":2,"pageIndex":2,"pageCount":49,"recordCount":97,"pageFirstRecordNumber":2},
     "return":
     [{"id":353023,"title":"0022010","groups":0,"password":"bg2rFZ2PEX","dateFrom":"2010-01-02","dateTo":null,"balanceMode":0,"paramGroupId":14,"personType":0,"comment":"","hidden":false,"superCid":0,"dependSubList":"","status":0,"statusTimeChange":"2010-01-13","titlePatternId":0,"balanceSubMode":0,"sub":false,"independSub":false,"balanceLimit":0.00,"super":false,"dependSub":false},
     {"id":353209,"title":"06-10-10/И-Г/0","groups":0,"password":"9351220759","dateFrom":"2010-10-06","dateTo":null,"balanceMode":1,"paramGroupId":14,"personType":0,"comment":"","hidden":false,"superCid":0,"dependSubList":"","status":0,"statusTimeChange":"2010-10-06","titlePatternId":0,"balanceSubMode":0,"sub":false,"independSub":false,"balanceLimit":0.00,"super":false,"dependSub":false}]}}

 Примеры.
  
 Извлечение массива:
  TypeTreeItem[] childs = TransferData.m.readValue( node.traverse(), TypeTreeItem[].class );
 
 Преобразование в тип: 
  TypeTreeItem childItem = jsonMapper.convertValue( childNode, TypeTreeItem.class );
 
 Получение как List:
  readJsonValue( transferData.postDataReturn( req, user ).traverse(),  
			     jsonTypeFactory.constructCollectionType( List.class, IdTitle.class ) )

 Ссылки:
  http://www.bgbilling.ru/v6.1/doc/ch02s08.html
  http://wiki.fasterxml.com/JacksonInFiveMinutes    
     
*/
public class RequestJsonRpc
{
	private final String module;
	private final int moduleId;
	private final String service;
	private final String method;
	
	private final Map<String, Object> params = new HashMap<String, Object>();
	
	public RequestJsonRpc( String module, String service, String method )
	{
		this( module, 0, service, method );
	}
	
	public RequestJsonRpc( String module, int moduleId, String service, String method )
	{
		this.module = module;
		this.moduleId = moduleId;
		this.service = service;
		this.method = method;
	}
	
	public String getUrl()
	{
		StringBuilder result = new StringBuilder( module );
		
		if( moduleId > 0 )
		{
			result.append( "/" );
			result.append( moduleId );
		}
		result.append( "/" );
		result.append( service );
		
		return result.toString();
	}
	
	public String getMethod()
	{
		return method;
	}
	
	public void setParam( String name, Object value )
	{
		params.put( name, value );
	}
	
	public void setParamContractId( int value )
	{
		params.put( "contractId", value );
	}
	
	public Map<String, Object> getParams()
	{
		return params;
	}
}