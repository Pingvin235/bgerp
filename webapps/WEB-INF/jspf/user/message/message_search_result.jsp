<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="customerLinkRoleConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.customer.config.ProcessLinkModesConfig')}"/>

	<table class="data mt05">
		<tr>
			<td>&nbsp;</td>
			<td>ID</td>
			<td>${l.l('Тип')}</td>
			<td width="100%">${l.l('Наименование')}</td>
		</tr>

		<c:forEach var="item" items="${form.response.data.searchedList}" varStatus="status">
			<c:set var="item" value="${item}" scope="request"/>

			<tr>
				<td>
					<input type="checkbox" name="link" value="${item.linkedObjectType}*${item.linkedObjectId}*${fn:escapeXml(item.linkedObjectTitle)}">
				</td>
				<td>${item.linkedObjectId}</td>

				<c:set var="customerLinkRole" value="${customerLinkRoleConfig.modeMap[item.linkedObjectType]}"/>

				<c:if test="${not empty customerLinkRole}">
					<td>${customerLinkRole}</td>
					<td><a href="#" onclick="openCustomer(${item.linkedObjectId}); return false;">${fn:escapeXml(item.linkedObjectTitle)}</a></td>
				</c:if>

				<c:set var="endpoint" value="user.message.search.result.jsp"/>
				<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
			</tr>
		</c:forEach>
	</table>
</u:sc>