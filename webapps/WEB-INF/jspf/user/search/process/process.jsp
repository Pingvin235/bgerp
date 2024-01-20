<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="common.jsp"%>

<table class="data mt1">
	<c:choose>
		<c:when test="${form.param.mode eq MODE_USER_CREATED}">
			<tr>
				<td width="30">ID</td>
				<td width="120">${l.l('Creation time')}</td>
				<td>${l.l('Description')}</td>
			</tr>
			<c:forEach var="item" items="${frd.list}">
			<tr>
				<td><ui:process-link id="${item.id}"/></td>
				<td nowrap="nowrap">${tu.format(item.createTime, 'ymdhms')}</td>
				<td>${item.description}
					<c:if test="${not empty item.reference}">(${item.reference})</c:if>
				</td>
			</tr>
			</c:forEach>
		</c:when>
		<c:when test="${form.param.mode eq MODE_USER_CLOSED}">
			<tr>
				<td width="30">ID</td>
				<td width="120">${l.l('Время закрытия')}</td>
				<td>${l.l('Description')}</td>
			</tr>
			<c:forEach var="item" items="${frd.list}">
			<tr>
				<td><ui:process-link id="${item.id}"/></td>
				<td nowrap="nowrap">${tu.format(item.closeTime, 'ymdhms')}</td>
				<td>${item.description}
					<c:if test="${not empty item.reference}">(${item.reference})</c:if>
				</td>
			</tr>
			</c:forEach>
		</c:when>
		<c:when test="${form.param.mode eq MODE_USER_STATUS_CHANGED}">
			<tr>
				<td width="30">ID</td>
				<td width="120">${l.l('Время изменения')}</td>
				<td width="120">${l.l('Status')}</td>
				<td>${l.l('Description')}</td>
			</tr>
			<c:forEach var="item" items="${frd.list}">
			<tr>
				<td><ui:process-link id="${item.id}"/></td>
				<td nowrap="nowrap">${tu.format(item.statusTime, 'ymdhms')}</td>
				<td>${ctxProcessStatusMap[item.statusId].title}</td>
				<td>${item.description}
					<c:if test="${not empty item.reference}">(${item.reference})</c:if>
				</td>
			</tr>
			</c:forEach>
		</c:when>
	</c:choose>
</table>