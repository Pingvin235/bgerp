<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<table class="data mt05">
		<tr>
			<td>&nbsp;</td>
			<td>ID</td>
			<td>${l.l('Type')}</td>
			<td width="100%">${l.l('Title')}</td>
		</tr>

		<c:set var="customerLinkRoleConfig" value="${ctxSetup.getConfig('ProcessCustomerLinkRoleConfig')}"/>

		<c:set var="customer" value="${frd.customer}"/>
		<c:choose>
			<c:when test="${not empty customer}">
				<tr>
					<td>
						<input type="checkbox" checked="true" disabled="true" name="link" value="customer*${customer.id}*${u.escapeXml(customer.title)}">
					</td>
					<td>${customer.id}</td>
					<td>${customerLinkRoleConfig.map['customer']}</td>
					<td><a href="#" onclick="$$.customer.open(${customer.id}); return false;">${u.escapeXml(customer.title)}</a></td>
				</tr>
			</c:when>
			<c:otherwise>
				<c:forEach var="item" items="${frd.searchedList}">
					<c:set var="item" value="${item}" scope="request"/>

					<tr>
						<td>
							<input type="checkbox" name="link" value="${item.linkObjectType}*${item.linkObjectId}*${u.escapeXml(item.linkObjectTitle)}">
						</td>
						<td>${item.linkObjectId}</td>

						<c:set var="customerLinkRole" value="${customerLinkRoleConfig.map[item.linkObjectType]}"/>
						<c:if test="${not empty customerLinkRole}">
							<td>${customerLinkRole}</td>
							<td><a href="#" onclick="$$.customer.open(${item.linkObjectId}); return false;">${u.escapeXml(item.linkObjectTitle)}</a></td>
						</c:if>

						<plugin:include endpoint="user.message.search.result.jsp"/>
					</tr>
				</c:forEach>
			</c:otherwise>
		</c:choose>
	</table>
</u:sc>