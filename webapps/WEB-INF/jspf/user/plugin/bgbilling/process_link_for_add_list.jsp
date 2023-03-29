<%@page import="ru.bgcrm.model.process.Process"%>
<%@page import="ru.bgcrm.cache.ProcessTypeCache"%>
<%@page import="ru.bgcrm.model.process.ProcessType"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="ru.bgcrm.plugin.bgbilling.DBInfoManager"%>
<%@page import="ru.bgcrm.util.Setup"%>
<%@page import="ru.bgcrm.util.sql.ConnectionSet"%>
<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="ru.bgcrm.dao.process.ProcessLinkDAO"%>
<%@page import="ru.bgcrm.dao.CustomerLinkDAO"%>
<%@page import="ru.bgcrm.model.CommonObjectLink"%>
<%@page import="ru.bgcrm.plugin.bgbilling.proto.model.Contract"%>
<%@page import="ru.bgcrm.struts.form.DynActionForm"%>
<%@page import="ru.bgcrm.model.Customer"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%
	ConnectionSet conSet = Setup.getSetup().getConnectionPool().getConnectionSet( false );
	try
	{
		ProcessLinkDAO processLinkDao = new ProcessLinkDAO( conSet.getConnection() );
		CustomerLinkDAO customerLinkDao = new CustomerLinkDAO( conSet.getConnection() );

		DynActionForm form = (DynActionForm)request.getAttribute( "form" );
		Process process = (Process)request.getAttribute( "process" );
		ProcessType type = ProcessTypeCache.getProcessType( process.getTypeId() );

		DBInfoManager dbM = DBInfoManager.getInstance();

		List<CommonObjectLink> linksForAdd = new ArrayList<CommonObjectLink>();
		pageContext.setAttribute( "linksForAdd", linksForAdd );

		CommonObjectLink customerLink = Utils.getFirst( processLinkDao.getObjectLinksWithType( form.getId(), Customer.OBJECT_TYPE + "%" ) );
		if( customerLink != null )
		{
			for( CommonObjectLink link : customerLinkDao.getObjectLinksWithType( customerLink.getLinkedObjectId(), Contract.OBJECT_TYPE + "%" ) )
			{
				// привязанный договор
				linksForAdd.add( link );
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
		<c:when test="${item.linkedObjectType.startsWith('contract:' )}">
			<c:set var="billingId" value="${su.substringAfter( item.linkedObjectType, ':')}"/>
			<c:set var="title" value="Договор:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}"/>
		</c:when>
		<c:when test="${item.linkedObjectType.startsWith('bgbilling-task:' )}">
			<c:set var="billingId" value="${su.substringAfter( item.linkedObjectType, ':')}"/>
			<c:set var="title" value="Задача:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}"/>
		</c:when>
	</c:choose>

	additionalLinksForAdd.push( {
		objectType: '${item.linkedObjectType}', id: ${item.linkedObjectId},
		title: '${item.linkedObjectTitle.replace("'", "&#8217;").replace("\"", "&#8220;")}', objectTypeTitle: '${title}' } );
</c:forEach>
