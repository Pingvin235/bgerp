package ru.bgcrm.plugin.bgbilling.proto.dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.bgbilling.DBInfo;
import ru.bgcrm.plugin.bgbilling.Request;
import ru.bgcrm.plugin.bgbilling.dao.BillingDAO;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo.ContractObjectModule;
import ru.bgcrm.plugin.bgbilling.proto.model.ContractObjectModuleInfo.ContractObjectModuleData;
import ru.bgcrm.plugin.bgbilling.proto.model.ParamAddressValue;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.call.Call;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.call.CallProblem;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.call.CallType;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.problem.Problem;
import ru.bgcrm.plugin.bgbilling.proto.model.crm.task.Task;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariff;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffGroup;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.ContractTariffOption;
import ru.bgcrm.plugin.bgbilling.proto.model.tariff.TariffGroup;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CrmDAO
	extends BillingDAO
{
	private static final String SERVICE_MODULE_ID = "service";
	private static final String CONTRACT_MODULE_ID = "contract";
	//private static final String ADMIN_MODULE_ID = "admin";
	private static final String CRM_MODULE_ID = "ru.bitel.bgbilling.plugins.crm";
	private static final String CONTRACT_OBJECT_MODULE_ID = "contract.object";

	public CrmDAO( User user, String billingId )
		throws BGException
	{
		super( user, billingId );
	}

	public CrmDAO( User user, DBInfo dbInfo )
		throws BGException
	{
		super( user, dbInfo );
	}

	public String createTask( int contractId, int objectId, int typeId, int groupId, int statusId,
							  String targetDate, String targetTime, String executeDate,
							  String executors, String resolution,
							  int addressParameterId, String comment )
		throws BGException
	{

		return updateTask( 0, contractId, objectId, typeId, groupId, statusId, targetDate, targetTime, executeDate, executors, resolution, addressParameterId, comment );
	}

	public Task getTask( int taskId )
		throws BGException
	{
		Request req = new Request();

		req.setModule( CRM_MODULE_ID );
		req.setAction( "GetRegisterTask" );
		req.setAttribute( "id", taskId );

		Document document = transferData.postData( req, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "task" );

		Element element = (Element)nodeList.item( 0 );

		if( !element.hasAttributes() )
			return new Task();

		Task task = new Task();
		task.setId( taskId );
		task.setExecuteDate( element.getAttribute( "execute_date" ) );
		task.setStatusCode( Utils.parseInt( element.getAttribute( "status" ) ) );
		task.setTargetDateTime( element.getAttribute( "target_date_and_time" ) );
		task.setTypeId( Utils.parseInt( element.getAttribute( "type" ) ) );
		task.setExecutors( element.getAttribute( "executors" ) );
		task.setGroupId( Utils.parseInt( element.getAttribute( "group" ) ) );
		task.setContract( element.getAttribute( CONTRACT_MODULE_ID ) );
		task.setContractId( Utils.parseInt( element.getAttribute( "contract_id" ) ) );
		task.setAddressPId(  Utils.parseInt( element.getAttribute( "apid" ) ) );
		task.setObjectId( Utils.parseInt( element.getAttribute( "aObjectId" ) ) );

		if( element.getAttribute( "open" ).length() > 0 )
		{
			task.setOpenDate( element.getAttribute( "open" ).split( "-" )[0] );
			task.setOpenUser( element.getAttribute( "open" ).split( "-" )[1] );
		}

		NodeList commentData = element.getElementsByTagName( "comment" );
		if( commentData.getLength() > 0 )
		{
			NodeList comments = ((Element)commentData.item( 0 )).getElementsByTagName( "row" );
			StringBuilder comment = new StringBuilder();
			for( int index = 0; index < comments.getLength(); index++ )
			{
				Element text = (Element)comments.item( index );
				comment.append( text.getAttribute( "text" ) );
			}
			task.setComment( comment.toString() );
		}

		NodeList resolutionData = element.getElementsByTagName( "resolution" );
		if( resolutionData.getLength() > 0 )
		{
			NodeList resolutions = ((Element)resolutionData.item( 0 )).getElementsByTagName( "row" );
			StringBuilder resolution = new StringBuilder();
			for( int index = 0; index < resolutions.getLength(); index++ )
			{
				Element text = (Element)resolutions.item( index );
				resolution.append( text.getAttribute( "text" ) );
			}
			task.setResolution( resolution.toString() );
		}

		Task.Log log = task.getLog();
		log.setOpen( element.getAttribute( "open" ) );
		log.setClose( element.getAttribute( "close" ) );
		log.setLastModify( element.getAttribute( "lastmod" ) );
		log.setAccept( element.getAttribute( "accept" ) );

		return task;
	}

    public String updateTask( Task task )
    throws BGException
    {

        return this.updateTask( task.getId(), task.getContractId(), task.getObjectId(),
                                task.getTypeId(), task.getGroupId(), task.getStatusCode(), task.getExecuteDate(),
                                task.getExecuteDate(), task.getExecuteDate(), task.getExecutors(),
                                task.getResolution(), task.getAddressPId(), task.getComment() );

    }

	public String updateTask( int taskId, int contractId, int objectId, int typeId, int groupId, int statusId,
							  String targetDate, String targetTime, String executeDate,
							  String executors, String resolution,
							  int addressParameterId, String comment )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAttribute( "action", "UpdateRegisterTask" );
		request.setAttribute( "type", typeId );
		request.setAttribute( "cid", contractId );
		request.setAttribute( "comment", comment );
		request.setAttribute( "group", groupId );

		if( taskId == 0 )
		{
			request.setAttribute( "id", "new" );
		}
		else
		{
			request.setAttribute( "id", taskId );
		}
		request.setAttribute( "status", statusId );
		if( Utils.notBlankString( targetDate ) || Utils.notBlankString( targetTime ) )
		{
			request.setAttribute( "target_date_and_time", targetDate + " " + targetTime );
		}
		if( Utils.notBlankString( executeDate ) )
		{
			request.setAttribute( "execute_date", executeDate );
		}
		if( Utils.notBlankString( executors ) )
		{
			request.setAttribute( "executors", executors );
		}
		if( Utils.notBlankString( resolution ) )
		{
			request.setAttribute( "resolution", resolution );
		}
		if( objectId != 0 )
		{
			request.setAttribute( "aObjectId", objectId );
		}

		request.setAttribute( "apid", addressParameterId );

		Document doc = transferData.postData( request, user );

		NodeList nodeList = doc.getElementsByTagName( "task" );
		for( int i = 0; i < nodeList.getLength(); i++ )
		{
			Node node = nodeList.item( i );
			if( node.getNodeType() == Node.ELEMENT_NODE )
			{
				Element element = (Element)node;
				return element.getAttribute( "id" );
			}
		}

		return "0";
	}

	/**
	 * Функция создаёт звонок в плагине биллинга "CRM"
	 * @param contractId ID договора к которому будет привязан звонок
	 * @param subjectId ID типа звонка
	 * @param groupId ID группы, на которую будет зарегистрирован звонок и проблема
	 * @param problemId ID проблемы к которой будет привязан звонок, если problemId <= 0, то будет создана новая проблема 
	 * @param comment комментарий к звонку
	 * @param problemComment комментарий к проблеме
	 * @return ID проблемы, созданной по звонку, иначе -1
	 * @throws BGException
	 */
	public int updateRegisterCall( int contractId,
								   int subjectId,
								   int groupId,
								   int problemId,
								   String comment,
								   String problemComment )
		throws BGException
	{
		int result = -1;

		Request request = new Request();
		request.setModule( "ru.bitel.bgbilling.plugins.crm" );
		request.setAction( "UpdateRegisterCall" );
		request.setContractId( contractId );
		request.setAttribute( "id", "new" );
		request.setAttribute( "subject", subjectId );
		request.setAttribute( "group", groupId );

		if( !Utils.isEmptyString( comment ) )
		{
			request.setAttribute( "comment", comment );
		}

		if( !Utils.isEmptyString( problemComment ) )
		{
			request.setAttribute( "problem_comment", problemComment );
		}

		if( problemId > 0 )
		{
			request.setAttribute( "problem_id", problemId );
		}

		Document document = transferData.postData( request, user );
		Element rootElement = document.getDocumentElement();

		if( "ok".equals( rootElement.getAttribute( "status" ) ) )
		{
			result = Utils.parseInt( rootElement.getAttribute( "problem_id" ), 0 );
		}

		return result;
	}

	public void updateRegisterProblemShort( int problemId,
											int status,
											String comment )
		throws BGException
	{
		Request request = new Request();
		request.setModule( "ru.bitel.bgbilling.plugins.crm" );
		request.setAction( "UpdateRegisterProblemShort" );
		request.setAttribute( "id", problemId );
		request.setAttribute( "comment", comment );
		request.setAttribute( "status", String.valueOf( status ) );

		Document document = transferData.postData( request, user );
		Element rootElement = document.getDocumentElement();

		if( !"ok".equals( rootElement.getAttribute( "status" ) ) )
		{
			throw new BGException( transferData.getMessage() );
		}
	}

	//без пролистывания
	public List<Call> getCallList( int contractId )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "RegisterCallTable" );
		request.setContractId( contractId );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "row" );
		List<Call> callList = new ArrayList<Call>();

		final int length = nodeList.getLength();
		for( int index = 0; index < length; index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			Call call = new Call();
			call.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			call.setContract( rowElement.getAttribute( CONTRACT_MODULE_ID ) );
			call.setContractId( Utils.parseInt( rowElement.getAttribute( "contract_id" ) ) );
			call.setProblem( rowElement.getAttribute( "problem" ) );
			call.setTime( rowElement.getAttribute( "time" ) );
			call.setType( rowElement.getAttribute( "type" ) );
			call.setUser( rowElement.getAttribute( "user" ) );

			callList.add( call );
		}

		return callList;
	}

	//с возможностью пролистывания
	public void getCallList( SearchResult<Call> result, int contractId )
		throws BGException
	{
		int pageIndex = result.getPage().getPageIndex();
		int pageSize = result.getPage().getPageSize();

		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "RegisterCallTable" );
		request.setContractId( contractId );
		request.setAttribute( "pageSize", pageSize );
		request.setAttribute( "pageIndex", pageIndex );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "row" );
		List<Call> callList = result.getList();

		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			Call call = new Call();
			call.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			call.setContract( rowElement.getAttribute( CONTRACT_MODULE_ID ) );
			call.setContractId( Utils.parseInt( rowElement.getAttribute( "contract_id" ) ) );
			call.setProblem( rowElement.getAttribute( "problem" ) );
			call.setTime( rowElement.getAttribute( "time" ) );
			call.setType( rowElement.getAttribute( "type" ) );
			call.setUser( rowElement.getAttribute( "user" ) );

			callList.add( call );
		}

		NodeList table = dataElement.getElementsByTagName( "table" );
		if( table.getLength() > 0 )
		{
			result.getPage().setRecordCount( Utils.parseInt( ((Element)table.item( 0 )).getAttribute( "recordCount" ) ) );
			result.getPage().setPageCount( Utils.parseInt( ((Element)table.item( 0 )).getAttribute( "pageCount" ) ) );
		}
	}

	public List<CallType> getCallRegisterTypeList()
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "RegisterSubjectList" );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "item" );

		List<CallType> callRegisterTypeList = new ArrayList<CallType>();
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			CallType type = new CallType();
			type.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			type.setTitle( rowElement.getAttribute( "title" ) );

			callRegisterTypeList.add( type );
		}

		return callRegisterTypeList;
	}

	public List<IdTitle> getRegisterGroupList()
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "RegisterGroupList" );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "item" );

		List<IdTitle> registerGroupList = new ArrayList<IdTitle>();
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			IdTitle type = new IdTitle();
			type.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			type.setTitle( rowElement.getAttribute( "title" ) );

			registerGroupList.add( type );
		}

		return registerGroupList;
	}

	public int getRegisterSubjectGroup( int typeId )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "GetRegisterSubjectGroup" );
		request.setAttribute( "subject", typeId );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();

		return Utils.parseInt( dataElement.getAttribute( "gid" ) );
	}

	public List<IdTitle> getRegisterExecutorList( String groupId )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "RegisterExecutorList" );

		if( Utils.notBlankString( groupId ) )
		{
			request.setAttribute( "groups", groupId );
		}

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "item" );

		List<IdTitle> registerExecutorList = new ArrayList<IdTitle>();
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			IdTitle executor = new IdTitle();
			executor.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			executor.setTitle( rowElement.getAttribute( "title" ) );

			registerExecutorList.add( executor );
		}

		return registerExecutorList;
	}

	public List<CallProblem> getCallCurrentProblemList( int contractId )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "CurrentProblemList" );
		request.setContractId( String.valueOf( contractId ) );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "item" );

		List<CallProblem> callCurrentProblemList = new ArrayList<CallProblem>();
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			CallProblem type = new CallProblem();
			type.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			type.setTitle( rowElement.getAttribute( "title" ) );

			callCurrentProblemList.add( type );
		}

		return callCurrentProblemList;
	}

	public List<IdTitle> getTaskTypeList()
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "ListDirectory" );
		request.setAttribute( "mode", 16 );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "item" );

		List<IdTitle> directoryList = new ArrayList<IdTitle>();
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			IdTitle directory = new IdTitle();
			directory.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			directory.setTitle( rowElement.getAttribute( "title" ) );

			directoryList.add( directory );
		}

		return directoryList;
	}

	public void getTaskList( SearchResult<Task> result, int contractId, String sort1, String sort2 )
		throws BGException
	{
		searchTask( result, contractId, null, null, null, null, null, null, null, null, null, sort1, sort2 );
	}

	public void getTaskList( SearchResult<Task> result, int contractId )
		throws BGException
	{
		searchTask( result, contractId, null, null, null, null, null, null, null, null, null, null, null );
	}

	public void getTaskList( SearchResult<Task> result, int contractId, List<Integer> statusIds )
		throws BGException
	{
		searchTask( result, contractId, null, null, statusIds, null, null, null, null, null, null, null, null );
	}

	public void searchTask( SearchResult<Task> result, int contractId,
							List<Integer> typeIds, List<Integer> groupIds, List<Integer> statusIds,
							Date createDateFrom, Date createDateTo,
							Date closeDateFrom, Date closeDateTo,
							Date acceptDateFrom, Date acceptDateTo,
							String sort1, String sort2 )
		throws BGException
	{
		int pageIndex = result.getPage().getPageIndex();
		int pageSize = result.getPage().getPageSize();

		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "GetRegisterTaskTable" );
		request.setContractId( contractId );
		request.setAttribute( "pageSize", pageSize );
		request.setAttribute( "pageIndex", pageIndex );
		if( statusIds != null && statusIds.size() > 0 )
		{
			request.setAttribute( "status", Utils.toString( statusIds ) );
		}

		if( typeIds != null && typeIds.size() > 0 )
		{
			for( int typeId : typeIds )
			{
				request.setAttribute( "type", typeId );
			}
		}

		if( groupIds != null && groupIds.size() > 0 )
		{
			request.setAttribute( "groups", Utils.toString( groupIds ) );
		}

		SimpleDateFormat format = new SimpleDateFormat( "dd.MM.yyyy" );
		if( createDateFrom != null ) request.setAttribute( "date_type=open_dt&date1", format.format( createDateFrom ) );
		if( createDateTo != null ) request.setAttribute( "date_type=open_dt&date2", format.format( createDateTo ) );
		if( acceptDateFrom != null ) request.setAttribute( "date_type=accept_dt&date1", format.format( acceptDateFrom ) );
		if( acceptDateTo != null ) request.setAttribute( "date_type=accept_dt&date2", format.format( acceptDateTo ) );
		if( closeDateFrom != null ) request.setAttribute( "date_type=close_dt&date1", format.format( createDateFrom ) );
		if( closeDateTo != null ) request.setAttribute( "date_type=close_dt&date2", format.format( createDateTo ) );

		if( sort1 != null )
		{
			request.setAttribute( "sort1", sort1 );
		}

		if( sort2 != null )
		{
			request.setAttribute( "sort2", sort2 );
		}

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "row" );
		List<Task> taskList = result.getList();

		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			Task task = new Task();
			task.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			task.setComment( rowElement.getAttribute( "comment" ) );
			task.setContract( rowElement.getAttribute( CONTRACT_MODULE_ID ) );
			task.setPhones( rowElement.getAttribute( "phone" ) );
			task.setContractComment( rowElement.getAttribute( "contract_comment" ) );
			task.setCurrentDate( rowElement.getAttribute( "current_date" ) );
			task.setExecuteDate( rowElement.getAttribute( "execute_date" ) );
			task.setExecutors( rowElement.getAttribute( "executors" ) );
			task.setFio( rowElement.getAttribute( "fio" ) );
			task.setGroupTitle( rowElement.getAttribute( "group" ) );
			task.setOpenDate( rowElement.getAttribute( "open_date" ) );
			task.setOpenUser( rowElement.getAttribute( "open_user" ) );
			task.setResolution( rowElement.getAttribute( "resolution" ) );
			task.setStatus( rowElement.getAttribute( "status" ) );
			task.setStatusCode( Utils.parseInt( rowElement.getAttribute( "status_code" ) ) );
			task.setTypeTitle(  rowElement.getAttribute( "type" ) );
			task.setTargetDateTime( rowElement.getAttribute( "target_date" ) );

			ParamAddressValue address = new ParamAddressValue();
			address.setAreaTitle( rowElement.getAttribute( "area" ) );
			address.setCityTitle( rowElement.getAttribute( "city" ) );
			address.setFlat( rowElement.getAttribute( "flat" ) );
			address.setHouse( rowElement.getAttribute( "house" ) );
			address.setPod( rowElement.getAttribute( "pod" ) );
			address.setQuarterTitle( rowElement.getAttribute( "quarter" ) );
			address.setStreetTitle( rowElement.getAttribute( "street" ) );
			task.setAddress( ParamAddressValue.buildAddressValue( address ) );

			taskList.add( task );
		}

		NodeList table = dataElement.getElementsByTagName( "table" );
		if( table.getLength() > 0 )
		{
			result.getPage().setRecordCount( Utils.parseInt( ((Element)table.item( 0 )).getAttribute( "recordCount" ) ) );
			result.getPage().setPageCount( Utils.parseInt( ((Element)table.item( 0 )).getAttribute( "pageCount" ) ) );
		}
	}

	public String updateRegisterProblem( int problemId, int statusId, int groupId, String executors, String comment, int urgency )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAttribute( "action", "UpdateRegisterProblem" );
		request.setAttribute( "urgency", urgency );
		request.setAttribute( "comment", comment );
		request.setAttribute( "group", groupId );

		if( problemId == 0 )
		{
			request.setAttribute( "id", "new" );
		}
		else
		{
			request.setAttribute( "id", problemId );
		}
		request.setAttribute( "status", statusId );

		if( Utils.notBlankString( executors ) )
		{
			request.setAttribute( "executors", executors );
		}

		Document doc = transferData.postData( request, user );

		NodeList nodeList = doc.getElementsByTagName( "problem" );
		for( int i = 0; i < nodeList.getLength(); i++ )
		{
			Node node = nodeList.item( i );
			if( node.getNodeType() == Node.ELEMENT_NODE )
			{
				Element element = (Element)node;
				return element.getAttribute( "id" );
			}
		}

		return "0";
	}

	public void processTask( String email, String subject, List<Integer> taskIds )
		throws BGException
	{
		Request request = new Request();

		request.setModule( "ru.bitel.bgbilling.plugins.crm" );
		request.setAction( "SyncProcessRegisterTask" );
		request.setAttribute( "mail", email );
		request.setAttribute( "subject", subject );
		request.setAttribute( "id_list", Utils.toString( taskIds, "", "," ) );

		transferData.postData( request, user );
	}

	public void processTaskSync( String email, String subject, List<Integer> taskIds )
		throws BGException
	{
		Request request = new Request();
		request.setModule( "ru.bitel.bgbilling.plugins.crm" );
		request.setAction( "SyncProcessRegisterTask" );
		request.setAttribute( "mail", email );
		request.setAttribute( "subject", subject );
		request.setAttribute( "id_list", Utils.toString( taskIds, "", "," ) );

		transferData.postDataSync( request, user );
	}


	public Problem getRegisterProblem( int processId )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CRM_MODULE_ID );
		request.setAction( "RegisterProblemTable" );
		request.setAttribute( "id", processId );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "row" );

		if( nodeList.getLength() > 0 )
		{
			Element rowElement = (Element)nodeList.item( 0 );
			Problem problem = new Problem();
			problem.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			problem.setComment( rowElement.getAttribute( "comment" ) );
			problem.setResolution( rowElement.getAttribute( "resolution" ) );
			problem.setStatusTitle( rowElement.getAttribute( "status" ) );
			problem.setStatusCode( Utils.parseInt( rowElement.getAttribute( "status_code" ) ) );
			problem.setDuration( rowElement.getAttribute( "duration" ) );
			problem.setGroupTitle( rowElement.getAttribute( "group" ) );
			problem.setStatusDate( rowElement.getAttribute( "status_time" ) );
			problem.setStatusUser( rowElement.getAttribute( "status_user" ) );
			problem.setUrgency( Utils.parseInt( rowElement.getAttribute( "urgency" ).split( ";" )[1] ) );

			//доп инфа
			request = new Request();
			request.setModule( CRM_MODULE_ID );
			request.setAction( "GetRegisterProblem" );
			request.setAttribute( "id", processId );

			document = transferData.postData( request, user );

			dataElement = document.getDocumentElement();
			nodeList = dataElement.getElementsByTagName( "problem" );

			if( nodeList.getLength() > 0 )
			{
				rowElement = (Element)nodeList.item( 0 );
				problem.setExecutors( rowElement.getAttribute( "executors" ) );
				problem.setGroup( rowElement.getAttribute( "group" ) );
			}

			return problem;
		}

		return null;
	}

	public List<IdTitle> getBillingModuleList()
		throws BGException
	{
		Request request = new Request();
		request.setModule( SERVICE_MODULE_ID );
		request.setAction( "Modules" );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "module" );

		List<IdTitle> moduleList = new ArrayList<IdTitle>();
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			IdTitle module = new IdTitle();
			module.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			module.setTitle( rowElement.getAttribute( "title" ) );

			moduleList.add( module );
		}

		return moduleList;
	}


	public List<IdTitle> contractObjectTypeList()
		throws BGException
	{
		Request request = new Request();
		request.setModule( CONTRACT_OBJECT_MODULE_ID );
		request.setAction( "TypeList" );
		request.setAttribute( "onlyVisible", "1" );

		Document document = transferData.postData( request, user );

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "item" );

		List<IdTitle> objectTypeList = new ArrayList<IdTitle>();
		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			IdTitle type = new IdTitle();
			type.setId( Utils.parseInt( rowElement.getAttribute( "id" ) ) );
			type.setTitle( rowElement.getAttribute( "title" ) );

			objectTypeList.add( type );
		}

		return objectTypeList;
	}

	public ContractObjectModuleInfo contractObjectModuleList( int objectId )
		throws BGException
	{
		Request request = new Request();
		request.setModule( CONTRACT_OBJECT_MODULE_ID );
		request.setAction( "ObjectModuleTable" );
		request.setAttribute( "object_id", objectId );

		Document document = transferData.postData( request, user );

		ContractObjectModuleInfo moduleInfo = new ContractObjectModuleInfo();

		Element dataElement = document.getDocumentElement();
		NodeList nodeList = dataElement.getElementsByTagName( "row" );

		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			ContractObjectModuleData data = moduleInfo.new ContractObjectModuleData();

			data.setComment( rowElement.getAttribute( "comment" ) );
			data.setData( rowElement.getAttribute( "data" ) );
			data.setModule( rowElement.getAttribute( "module" ) );
			data.setPeriod( rowElement.getAttribute( "period" ) );

			moduleInfo.getModuleDataList().add( data );
		}

		nodeList = dataElement.getElementsByTagName( "module" );

		for( int index = 0; index < nodeList.getLength(); index++ )
		{
			Element rowElement = (Element)nodeList.item( index );
			ContractObjectModule data = moduleInfo.new ContractObjectModule();

			data.setId( objectId );
			data.setName( rowElement.getAttribute( "name" ) );
			data.setPackClient( rowElement.getAttribute( "pack_client" ) );
			data.setTitle( rowElement.getAttribute( "title" ) );

			moduleInfo.getModuleList().add( data );
		}

		return moduleInfo;
	}


	/**
	 * Использовать {@link ContractDAO#updateMemo(Integer, Integer, String, String, boolean)}.
	 */
	@Deprecated
	public void updateContractMemo( int contractId, int memoId, String memoTitle, String memoText, boolean visible )
  		throws BGException
  	{
		ContractDAO.getInstance( user, dbInfo ).updateMemo( contractId, memoId, memoTitle, memoText, visible );
    }

	/**
	 * Использовать {@link ContractTariffDAO#contractTariffList(Integer)}.
	 */
	@Deprecated
	public List<ContractTariff> contractGlobalTariffList( int contractId )
    	throws BGException
    {
		return new ContractTariffDAO( user, dbInfo ).contractTariffList( contractId );
    }

	/**
	 * Использовать {@link ContractTariffDAO#contractTariffOptionList(Integer)}.
	 */
	@Deprecated
	public List<ContractTariffOption> contractTariffOptionList( int contractId )
		throws BGException, ParseException
	{
		return new ContractTariffDAO( user, dbInfo ).contractTariffOptionList( contractId );
	}

	/**
	 * Использовать {@link ContractTariffDAO#contractActiveTariffGroup(Integer)}.
	 */
	@Deprecated
	public List<ContractTariffGroup> contractActiveTariffGroup( int contractId )
		throws BGException
	{
		return new ContractTariffDAO( user, dbInfo ).contractActiveTariffGroup( contractId );
	}

	/**
	 * Использовать {@link ContractTariffDAO#getContractTariffGroup(Integer)}.
	 */
	 public ContractTariffGroup getContractTariffGroup( int tariffGroupId )
     	throws BGException
     {
		 return new ContractTariffDAO( user, dbInfo ).getContractTariffGroup( tariffGroupId );
     }

	/**
     * Использовать {@link ContractTariffDAO#updateContractTariffPlan(Integer, Integer, Integer, Integer, String, String, String)}.
     */
	@Deprecated
	public void updateContractTariffPlan( int contractId, int tariffId, int tpid, int position, String dateFrom, String dateTo, String comment )
		throws BGException
	{
		new ContractTariffDAO( user, dbInfo ).updateContractTariffPlan( contractId, tariffId, tpid, position, dateFrom, dateTo, comment );
	}

	/**
     * Использовать {@link ContractTariffDAO#updateContractTariffPlan(Integer, Integer, Integer, Integer, String, String, String)}.
     */
	@Deprecated
	public void updateContractMemo( int contractId, int memoId, String memoTitle, String memoText )
		throws BGException
	{
		new ContractDAO( user, dbInfo ).updateMemo( contractId, memoId, memoTitle, memoText );
	}

	/**
     * Использовать {@link ContractTariffDAO#getTariffGroup(int)}.
     */
	@Deprecated
	public TariffGroup getTariffGroup( int groupId )
		throws BGException
	{
		return new DirectoryDAO( user, dbInfo ).getTariffGroup( groupId );
	}

	/**
     * Использовать {@link ContractDAO#groupsGet(int)}.
     */
	@Deprecated
	public List<Integer> getSelectedContractGroupIds( int contractId )
    	throws BGException
    {
    	List<Integer> result = new ArrayList<Integer>();
    	result.addAll( new ContractDAO( user, dbInfo ).groupsGet( contractId ).getSecond() );
    	return result;
    }

	/**
	 * Использовать {@link ContractDAO#additionalActionList(int)}.
	 */
	@Deprecated
	public List<IdTitle> additionalActionList( int contractId )
		throws BGException
	{
		return ContractDAO.getInstance( user, dbInfo ).additionalActionList( contractId );
	}

	/**
	 * Использовать {@link ContractDAO#executeAdditionalAction(int, int)}.
	 */
	@Deprecated
	public String executeAdditionalAction( int contractId, int actionId )
		throws BGException
	{
		return ContractDAO.getInstance( user, dbInfo ).executeAdditionalAction( contractId, actionId );
	}


	/**
	 * Использовать {@link ContractDAO#updateGroup(String, Integer, Integer)}.
	 */
	@Deprecated
	public void updateContractGroup( String command, int contractId, int groupId )
		throws BGException
	{
		ContractDAO.getInstance( user, dbInfo ).updateGroup( command, contractId, groupId );
	}
}
