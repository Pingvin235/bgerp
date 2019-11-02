<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty processType.properties.configMap['bgbilling:processShowLinkedProblems']}">
	<c:url var="url" value="link.do" >
		<c:param name="action" value="linkList"/>
		<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/problem/billing_problems.jsp"/>
		<c:param name="objectType" value="process"/>
		<c:param name="id" value="${form.id}"/>	
		<c:param name="processId" value="${process.id}"/>	
		<c:param name="linkedObjectType" value="bgbilling-problem"/>
		<c:if test="${not empty processType.properties.configMap['bgbilling:processLinkProblemSetProcessDescription']}">
			<%-- иначе просто в GET не влазит запрос --%>
			<c:param name="description" value="${fn:substring( process.description, 0, 1000 )}"/>
		</c:if>	
	</c:url>

	$tabs.tabs( "add", "${url}", "Проблемы в BGBilling" );
</c:if>

<c:set var="showInfo" value="${processType.properties.configMap['bgbilling:processShowLinkedContractsInfo']}"/>

<c:if test="${not empty showInfo}">
	<c:url var="url" value="link.do" >
		<c:param name="action" value="linkList"/>
		<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/process_contracts_info.jsp"/>
		<c:param name="objectType" value="process"/>
		<c:param name="id" value="${form.id}"/>
		<c:param name="linkedObjectType" value="contract"/>
		<c:param name="processTypeId" value="${process.typeId}"/>
		<c:param name="whatShow" value="${showInfo}"/>
	</c:url>
	
	$tabs.tabs( "add", "${url}", "Инфо по договорам" );
</c:if>
