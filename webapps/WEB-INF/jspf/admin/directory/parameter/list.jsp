<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<html:form action="admin/directory">
	<html:hidden property="action"/>
	<html:hidden property="directoryId"/>

	<%@ include file="../create_button.jsp"%>

	<ui:input-text name="filter" styleClass="ml1" value="${form.param['filter']}" placeholder="${l.l('Фильтр')}" size="40" 
		title="${l.l('По наименованию, комментарию, конфигурации')}"
		onSelect="openUrlContent( formUrl( this.form ) ); return false;"/>

	<button class="btn-grey ml1" type="button" onclick="openUrlContent( formUrl( this.form) )">=&gt;</button>

	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<c:if test="${not empty form.param.directoryId}">
	<table style="width: 100%;" class="data mt1">
		<tr>
			<td width="30">&#160;</td>
			<td width="30">ID</td>
			<td width="50">${l.l('Тип')}</td>
			<td width="50">${l.l('Порядок')}</td>
			<td width="35%">${l.l('Название')}</td>

			<td>${l.l('Скрипт')}</td>

			<td width="20%">${l.l('Комментарий')}</td>
			<td width="35%">${l.l('Конфигурация')}</td>
			<c:if test="${form.param.directoryId eq 'processParameter'}">
				<td width="20%">${l.l('Где используется')}</td>
			</c:if>
		</tr>

		<c:forEach var="item" items="${form.response.data.list}">
			<tr>
				<c:url var="editUrl" value="/admin/directory.do">
					<c:param name="action" value="parameterGet" />
					<c:param name="id" value="${item.id}" />
					<c:param name="returnUrl" value="${form.requestUrl}" />
					<c:param name="directoryId" value="${form.param.directoryId}" />
				</c:url>

				<c:url var="deleteAjaxUrl" value="/admin/directory.do">
					<c:param name="action" value="parameterDelete" />
					<c:param name="id" value="${item.id}" />
				</c:url>
				<c:url var="deleteAjaxCommandAfter"
					value="openUrlContent( '${form.requestUrl}' )" />

				<td nowrap="nowrap"><%@ include	file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
				<td align="right">${item.id}</td>
				<td>${item.type}</td>
				<td>${item.order}</td>
				<td>${fn:escapeXml( item.title )}</td>
				<td>${item.script}</td>
				<td>${fn:escapeXml( item.comment )}</td>
				<td>
					<c:set var="maxLength" value="100"/>
					<c:if test="${maxLength gt 0}">
						<c:set var="text" value="${item.config}"/>
						<%@include file="/WEB-INF/jspf/short_text.jsp"%>
					</c:if>
				</td>

				<c:if test="${form.param.directoryId eq 'processParameter'}">
					<c:set var="showId" value="${u:uiid()}" />
					<c:set var="parameterId" value="${item.id}" />

						<td align="center">
								<c:url var="showUrl" value="/admin/directory.do">
									<c:param name="action" value="parameterUseProcess" />
									<c:param name="parameterId" value="${parameterId}" />
								</c:url>

								<button type="button" class="btn-white btn-small" style="width: 100%" onclick="$(this).hide(); openUrlTo('${showUrl}', $('#${showId}') );">${l.l('Показать')}</button>

								<div id="${showId}">
								</div>
						</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
</c:if>

<c:set var="state" value=""/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
