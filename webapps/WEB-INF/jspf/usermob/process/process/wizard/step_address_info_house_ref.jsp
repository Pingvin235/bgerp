<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="house" value="${frd.house}"/>

<%-- нестандартные параметры, в скором времени будут заменены на систему параметров --%>
<c:set var="additionalParams" value="${not empty setup['address.house.additional.params'] }"/>

<table style="width: 100%;" class="oddeven">
	<tr>
		<td>Коментарий</td>
		<td>${house.comment}</td>
	</tr>
	<c:if test="${additionalParams}">
		<tr>
			<td>Коментарий Интернет</td>
			<td>${house.config['billing.service.type.internet']}</td>
		</tr>
		<tr>
			<td>Коментарий КТВ</td>
			<td>${house.config['billing.service.type.tv']}</td>
		</tr>
	</c:if>
</table>