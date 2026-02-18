<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${processType.properties.configMap['getolt:processShowTab'] eq 1 and ctxUser.checkPerm('/user/plugin/getolt/getolt:null')}">
	<c:url var="url" value="/user/plugin/getolt/getolt.do">
		<c:param name="processId" value="${process.id}" />
	</c:url>
	$tabs.tabs('add', "${url}", "GetOLT");
</c:if>
