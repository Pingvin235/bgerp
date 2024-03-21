<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="customerLinkRoleConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.customer.config.ProcessLinkModesConfig')}"/>

	<table class="data mt05">
		<tr>
			<td>&nbsp;</td>
			<td>ID</td>
			<td>${l.l('Type')}</td>
			<td width="100%">${l.l('Title')}</td>
		</tr>

		<c:forEach var="item" items="${frd.searchedList}" varStatus="status">
			<c:set var="item" value="${item}" scope="request"/>

			<tr>
				<td>
					<input type="checkbox" name="link" value="${item.linkObjectType}*${item.linkObjectId}*${u.escapeXml(item.linkObjectTitle)}">
				</td>
				<td>${item.linkObjectId}</td>

				<c:set var="customerLinkRole" value="${customerLinkRoleConfig.modeMap[item.linkObjectType]}"/>

				<c:if test="${not empty customerLinkRole}">
					<td>${customerLinkRole}</td>
					<td><a href="#" onclick="$$.customer.open(${item.linkObjectId}); return false;">${u.escapeXml(item.linkObjectTitle)}</a></td>
				</c:if>

				<plugin:include endpoint="user.message.search.result.jsp"/>
			</tr>
		</c:forEach>
	</table>
</u:sc>