<%@page import="ru.bgcrm.model.process.Process"%>
<%@page import="org.bgerp.cache.ProcessTypeCache"%>
<%@page import="ru.bgcrm.model.process.ProcessType"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="ru.bgcrm.plugin.bgbilling.DBInfoManager"%>
<%@page import="org.bgerp.app.cfg.Setup"%>
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
			for( CommonObjectLink link : customerLinkDao.getObjectLinksWithType( customerLink.getLinkObjectId(), Contract.OBJECT_TYPE + "%" ) )
			{
				// привязанный договор
				linksForAdd.add( link );
			}
		}
	}
	finally
	{
		conSet.close();
	}
%>

<c:forEach var="item" items="${linksForAdd}">
	<c:if test="${item.linkObjectType.startsWith('contract:' )}">
		<c:set var="billingId" value="${su.substringAfter(item.linkObjectType, ':')}"/>
		<c:set var="title" value="Договор: ${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}"/>
	</c:if>

	additionalLinksForAdd.push( {
		objectType: '${item.linkObjectType}', id: ${item.linkObjectId},
		title: '${item.linkObjectTitle.replace("'", "&#8217;").replace("\"", "&#8220;")}', objectTypeTitle: '${title}' } );
</c:forEach>
