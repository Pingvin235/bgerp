<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
<c:url var="url" value=""><c:param name="a" value="1"/></c:url>
--%>
 
<table style="width: 100%;">
	<tr>
		<td><h2>История изменения параметра (${parameter.title})</h2></td>
	<td align="right"><div style="margin: 5px 2px;"><input type="button" value="К списку параметров" onclick="document.location='${url}'"/></div></td>
</table>
<table style="width: 100%;">
	<tr>
		<td width="150">Дата</td>
		<td>Значение</td>
		<td width="300">Кто изменил</td>
	</tr>
	<c:forEach var="item" items="${parameterHistoryList}" varStatus="">
	<c:set var="cl" value="odd"/><c:if test="${status.count mod 2 == 0}"><c:set var="cl" value="even"/></c:if>
	<tr>
		<td><fmt:formatDate value="${item.dateChanged}" pattern="dd.MM.yyyy HH:mm:ss"/></td>
		<td>${item.value}</td>
		<td>${item.userNameChanged}</td>
	</tr>
	</c:forEach>
</table>