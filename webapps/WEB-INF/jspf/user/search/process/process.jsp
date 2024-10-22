<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="common.jsp"%>

<table class="data mt1 hl">
	<c:choose>
		<c:when test="${form.param.mode eq MODE_USER_CREATED}">
			<tr>
				<td>${l.l('Process')}</td>
				<td class="min">${l.l('Created')}</td>
			</tr>
			<c:forEach var="item" items="${frd.list}">
			<tr>
				<td><ui:process-link process="${item}"/></td>
				<td nowrap="nowrap">${tu.format(item.createTime, 'ymdhms')}</td>
			</tr>
			</c:forEach>
		</c:when>
		<c:when test="${form.param.mode eq MODE_USER_CLOSED}">
			<tr>
				<td>${l.l('Process')}</td>
				<td class="min">${l.l('Closed')}</td>
			</tr>
			<c:forEach var="item" items="${frd.list}">
			<tr>
				<td><ui:process-link process="${item}"/></td>
				<td nowrap="nowrap">${tu.format(item.closeTime, 'ymdhms')}</td>
			</tr>
			</c:forEach>
		</c:when>
		<c:when test="${form.param.mode eq MODE_USER_STATUS_CHANGED}">
			<tr>
				<td>${l.l('Process')}</td>
				<td class="min">${l.l('Status')}</td>
				<td class="min">${l.l('Modification time')}</td>
			</tr>
			<c:forEach var="item" items="${frd.list}">
			<tr>
				<td><ui:process-link process="${item}"/></td>
				<td>${item.statusTitle}</td>
				<td nowrap="nowrap">${tu.format(item.statusTime, 'ymdhms')}</td>
			</tr>
			</c:forEach>
		</c:when>
	</c:choose>
</table>