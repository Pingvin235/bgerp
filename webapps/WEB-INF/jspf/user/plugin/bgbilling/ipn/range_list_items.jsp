<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="url" value="${baseUrl}">
	<c:param name="action" value="rangeEdit"/>
</c:url>

<button class="btn-green mb1" onclick="$$.ajax.load('${url}', $('#${uiid}').parent())">+</button>

<table class="data" style="width: 100%;" id="${uiid}">
	<tr>
		<td></td>
		<td>${columnTitle}</td>
		<td>Период</td>
		<td>Источник</td>
		<td width="100%">Комментарий</td>
	</tr>

	<c:forEach var="item" items="${list}">
		<tr>
			<td nowrap="nowrap">
				<u:sc>
					<c:url var="url" value="${baseUrl}">
						<c:param name="action" value="rangeEdit"/>
						<c:param name="id" value="${item.id}"/>
					</c:url>
					<c:set var="editCommand" value="$$.ajax.load('${url}', $('#${uiid}').parent())"/>

					<c:url var="deleteAjaxUrl" value="${baseUrl}">
						<c:param name="action" value="rangeDelete"/>
						<c:param name="id" value="${item.id}"/>
					</c:url>
					<c:set var="deleteAjaxCommandAfter" value="$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent())"/>
					<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				</u:sc>
			</td>
			<td nowrap="nowrap">${item.addressRange}</td>
			<td nowrap="nowrap">${tu.formatPeriod( item.dateFrom, item.dateTo, 'ymd' )}</td>
			<td nowrap="nowrap">${item.ifaces}</td>
			<td>${item.comment}</td>
		</tr>
  	</c:forEach>
</table>
