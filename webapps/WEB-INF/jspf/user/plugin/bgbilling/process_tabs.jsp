<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="showInfo" value="${processType.properties.configMap['bgbilling:processShowLinkedContractsInfo']}"/>

<c:if test="${not empty showInfo}">
	<c:url var="url" value="link.do" >
		<c:param name="method" value="linkList"/>
		<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/process_contracts_info.jsp"/>
		<c:param name="objectType" value="process"/>
		<c:param name="id" value="${form.id}"/>
		<c:param name="linkedObjectType" value="contract"/>
		<c:param name="processTypeId" value="${process.typeId}"/>
		<c:param name="whatShow" value="${showInfo}"/>
	</c:url>

	$tabs.tabs( "add", "${url}", "Инфо по договорам" );
</c:if>
