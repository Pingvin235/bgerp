<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<c:url var="editUrl" value="/admin/config.do">
		<c:param name="action" value="get"/>
		<c:param name="id" value="${item.id}"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<c:url var="deleteUrl" value="/admin/config.do">
		<c:param name="action" value="delete"/>
		<c:param name="id" value="${item.id}"/>
	</c:url>

	<td nowrap="nowrap">
		<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${editUrl}', $$.shell.$content())"/>
		<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => { $$.ajax.load('${form.requestUrl}', $$.shell.$content()) })"/>
	</td>

	<td>${item.id}</td>
	<td style="text-align: center;"><c:if test="${item.active}"><i class="ti-check"></i></c:if></td>
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