<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<c:url var="editUrl" value="/admin/config.do">
		<c:param name="method" value="get"/>
		<c:param name="id" value="${item.id}"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<c:url var="deleteUrl" value="/admin/config.do">
		<c:param name="method" value="delete"/>
		<c:param name="id" value="${item.id}"/>
	</c:url>

	<td nowrap="nowrap">
		<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
		<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}').done(() => $$.ajax.loadContent('${form.requestUrl}', this))"/>
	</td>

	<td>${item.id}</td>
	<td style="text-align: center;"><c:if test="${item.active}"><i class="ti-check"></i></c:if></td>
	<td>${indent} ${item.title}</td>
	<td>${item.enabledPluginsTitles}</td>
	<td nowrap="true">
		<c:if test="${item.parentId ge 0 and item.active and ctxUser.checkPerm('/admin/config:addIncluded')}">
			<html:form action="/admin/config" style="display: none;">
				<input type="hidden" name="method" value="addIncluded"/>
				<input type="hidden" name="id" value="${item.id}"/>
				<ui:combo-single hiddenName="pluginId" prefixText="${l.l('Включённая конфигурация')}:">
					<jsp:attribute name="valuesHtml">
						<li value="">${l.l('Без плагина')}</li>
						<c:forEach var="plugin" items="${ctxPluginManager.inactivePluginList}">
							<li value="${plugin.id}">${l.l('Плагин')}&nbsp;${plugin.title}</li>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>

				<ui:button type="ok" styleClass="btn-grey ml1"
					onclick="$$.ajax.post(this).done(() => $$.ajax.loadContent('${form.requestUrl}', this))"/>
				<ui:button type="cancel" styleClass="ml1"
					onclick="const $td = $(this).closest('td'); $td.find('>button').show(); $td.find('>form').hide();"/>
			</html:form>

			<ui:button type="add" title="${l.l('Добавить включённый')}" styleClass="btn-small"
				onclick="const $td = $(this).closest('td'); $td.find('>button').hide(); $td.find('>form').show();"/>
		</c:if>
	</td>
</tr>

<c:if test="${not empty item.includedList}">
	<c:set var="indentBefore" value="${indent}"/>
	<c:set var="indent" scope="request" value="${indent}&nbsp;&nbsp;&nbsp;&nbsp;"/>
	<c:forEach var="item" items="${item.includedList}">
		<c:set var="item" scope="request" value="${item}"/>
		<jsp:include page="config_item.jsp"/>
	</c:forEach>
	<c:set var="indent" scope="request" value="${indentBefore}"/>
</c:if>