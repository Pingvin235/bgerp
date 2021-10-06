<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="pageFormId" value="searchForm-process" scope="request"/>
<c:set var="nextCommand" value="; openUrl( formUrl( $( '#searchForm-process' )[0] ), '#searchResult');" scope="request"/>

<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
<%@ include file="../process_search_constants.jsp"%>

<table class="data mt1">
	<c:choose>
		<c:when test="${form.param.mode eq MODE_USER_CREATED}">
			<tr>
				<td width="30">ID</td>
				<td width="120">${l.l('Время создания')}</td>
				<td>${l.l('Описание')}</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.list}">
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
				<td>${l.l('Описание')}</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.list}">
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
				<td width="120">${l.l('Статус')}</td>
				<td>${l.l('Описание')}</td>
			</tr>
			<c:forEach var="item" items="${form.response.data.list}">
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