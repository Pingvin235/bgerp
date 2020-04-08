<%@page import="ru.bgcrm.plugin.bgbilling.model.CommonContract"%>
<%@page import="ru.bgcrm.plugin.bgbilling.dao.CommonContractDAO"%>
<%@page import="java.util.Arrays"%>
<%@page import="ru.bgcrm.model.process.Process"%>
<%@page import="ru.bgcrm.cache.ProcessTypeCache"%>
<%@page import="ru.bgcrm.model.process.ProcessType"%>
<%@page import="ru.bgcrm.plugin.bgbilling.proto.model.crm.task.Task"%>
<%@page import="ru.bgcrm.model.SearchResult"%>
<%@page import="ru.bgcrm.plugin.bgbilling.proto.dao.CrmDAO"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="ru.bgcrm.plugin.bgbilling.DBInfoManager"%>
<%@page import="ru.bgcrm.plugin.bgbilling.Plugin"%>
<%@page import="ru.bgcrm.util.Setup"%>
<%@page import="ru.bgcrm.util.sql.ConnectionSet"%>
<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="ru.bgcrm.model.Customer"%>
<%@page import="ru.bgcrm.dao.process.ProcessLinkDAO"%>
<%@page import="java.sql.Connection"%>
<%@page import="ru.bgcrm.dao.CustomerLinkDAO"%>
<%@page import="ru.bgcrm.model.CommonObjectLink"%>
<%@page import="ru.bgcrm.plugin.bgbilling.proto.model.Contract"%>
<%@page import="ru.bgcrm.struts.form.DynActionForm"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%
	ConnectionSet conSet = Setup.getSetup().getConnectionPool().getConnectionSet( false );
	try
	{
		ProcessLinkDAO processLinkDao = new ProcessLinkDAO( conSet.getConnection() );
		CustomerLinkDAO customerLinkDao = new CustomerLinkDAO( conSet.getConnection() );
		CommonContractDAO commonContractDao = new CommonContractDAO( conSet.getConnection() );
		
		DynActionForm form = (DynActionForm)request.getAttribute( "form" );
		Process process = (Process)request.getAttribute( "process" );
		ProcessType type = ProcessTypeCache.getProcessType( process.getTypeId() );
		
		DBInfoManager dbM = DBInfoManager.getInstance();
		
		List<CommonObjectLink> linksForAdd = new ArrayList<CommonObjectLink>();
		pageContext.setAttribute( "linksForAdd", linksForAdd );
		
		CommonObjectLink customerLink = Utils.getFirst( processLinkDao.getObjectLinksWithType( form.getId(), Customer.OBJECT_TYPE + "%" ) );
		if( customerLink != null )
		{
			for( CommonContract cc : commonContractDao.getContractList( customerLink.getLinkedObjectId() ) )
			{
				CommonObjectLink link = new CommonObjectLink( Process.OBJECT_TYPE, process.getId(), CommonContract.OBJECT_TYPE, cc.getId(), cc.getFormatedNumber() );
				linksForAdd.add( link );
			}			
			
			for( CommonObjectLink link : customerLinkDao.getObjectLinksWithType( customerLink.getLinkedObjectId(), Contract.OBJECT_TYPE + "%" ) )
			{
				// привязанный договор	
				linksForAdd.add( link );
				
				// TODO: Удалить, старое.
				if( type.getProperties().getConfigMap().getBoolean( "bgbilling:offerTasksForLink", false ) )
				{				
					String billingId = StringUtils.substringAfter( link.getLinkedObjectType(), ":" );
					CrmDAO crmDao = new CrmDAO( form.getUser(), billingId );
					
					SearchResult<Task> taskList = new SearchResult<Task>();
					crmDao.getTaskList( taskList, link.getLinkedObjectId(), Arrays.asList( 0,1 ) );
					
					for( Task task : taskList.getList() )
					{
						CommonObjectLink taskLink = new CommonObjectLink();
						taskLink.setObjectId( form.getId() );
						taskLink.setLinkedObjectType( "bgbilling-task:" + billingId );
						taskLink.setLinkedObjectId( task.getId() );
						taskLink.setLinkedObjectTitle( link.getLinkedObjectTitle() + " => " + task.getTypeId() );
						
						linksForAdd.add( taskLink );
					}
				}
			}
		}
	}
	finally
	{
		conSet.recycle();	
	}
%>

<c:forEach var="item" items="${linksForAdd}">
	<c:remove var="prefix"/>
	<c:choose>
		<c:when test="${item.linkedObjectType eq 'bgbilling-commonContract'}">
			<c:set var="title" value="Единый договор"/>
		</c:when>
		<c:when test="${fn:startsWith( item.linkedObjectType, 'contract:' )}">
			<c:set var="billingId" value="${fn:substringAfter( item.linkedObjectType, ':')}"/>
			<c:set var="title" value="Договор:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}"/>
		</c:when>
		<c:when test="${fn:startsWith( item.linkedObjectType, 'bgbilling-task:' )}">
			<c:set var="billingId" value="${fn:substringAfter( item.linkedObjectType, ':')}"/>
			<c:set var="title" value="Задача:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}"/>
		</c:when>
	</c:choose>
	
	additionalLinksForAdd.push( {
		objectType: '${item.linkedObjectType}', id: ${item.linkedObjectId}, 
		title: '${item.linkedObjectTitle.replace("'", "&#8217;").replace("\"", "&#8220;")}', objectTypeTitle: '${title}' } );
</c:forEach>
