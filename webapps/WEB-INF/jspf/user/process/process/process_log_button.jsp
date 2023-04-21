<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="entityLogUrl" value="../user/parameter.do">
	<c:param name="action" value="entityLog"></c:param>
	<c:param name="id" value="${form.id}"></c:param>
	<c:param name="type" value="process"></c:param>
	<c:param name="returnUrl" value="${requestUrl}"></c:param>
</c:url>

<input type="button" onclick="$$.ajax.load('${entityLogUrl}', $('#${tableId}').parent());" value="${l.l('Лог изменений')}"/>