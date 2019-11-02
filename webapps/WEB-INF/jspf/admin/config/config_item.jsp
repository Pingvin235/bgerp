<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<c:url var="editUrl" value="/admin/config.do">
		<c:param name="action" value="get"/>
		<c:param name="id" value="${item.id}"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<c:url var="deleteAjaxUrl" value="/admin/config.do">
		<c:param name="action" value="delete"/>
		<c:param name="id" value="${item.id}"/>
	</c:url>
	<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>
	
	<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
	<td>${item.id}</td>
	<td style="text-align: center;"><c:if test="${item.active}">X</c:if></td>
	<td>${indent} ${item.title}</td>
</tr>

<c:if test="${not empty item.includedList}">
	<c:set var="indentBefore" value="${indent}"/>	
	<c:set var="indent" scope="request" value="${indent}&nbsp;&nbsp;&nbsp;&nbsp;"/>
	<c:forEach var="item" items="${item.includedList}">
		<c:set var="item" scope="request" value="${item}"/>
		<jsp:include page="config_item.jsp"/>		
	</c:forEach>
	<c:set var="indent" scope="request" value="${indentBefore}"/>
</c:if>