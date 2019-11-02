<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty form.response.data.groups}">
	<u:sc>
		<c:set var="id" value="processGroups"/>
		
		<c:set var="list" value="${form.response.data.groups}"/>
		<c:set var="hiddenName" value="groupId"/>
		<c:set var="prefixText" value="Группа решения:"/>
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
	</u:sc>
</c:if>