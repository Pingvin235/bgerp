<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="customerLinkRoleConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.customer.config.ProcessLinkModesConfig')}"/>

<h2>${l.l('Найдено')}: ${form.page.recordCount}, ${l.l('отображено')}: ${form.response.data.list.size()}</h2>

<table class="data">
	<tr>
		<td>&nbsp;</td>
		<td>ID</td>
		<td width="100%">${l.l('Title')}</td>
		<td>${l.l('Type')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td>
				<html:form action="/user/link">
					<input type="hidden" name="action" value="addLink"/>
					<input type="hidden" name="objectType" value="process"/>
					<input type="hidden" name="id" value="${form.param.processId}"/>
					<input type="hidden" name="linkedObjectId" value="${item.id}"/>
					<input type="hidden" name="linkedObjectTitle" value="${fn:escapeXml(item.title)}"/>
					<input type="hidden" name="linkedObjectType" value="customer"/>
					<input type="checkbox" name="check"/>
				</html:form>
			</td>
			<td>${item.id}</td>
			<td><ui:customer-link id="${item.id}" text="${item.title}"/></td>
			<td nowrap="nowrap">
				<ui:combo-single style="width: 100%;" list="${customerLinkRoleConfig.modeList}" onSelect="$$.process.link.customerRoleChanged($hidden)"/>
			</td>
		</tr>
	</c:forEach>
</table>


