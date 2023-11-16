<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<html:form action="/admin/directory">
	<html:hidden property="action"/>
	<html:hidden property="directoryId"/>

	<%@ include file="../create_button.jsp"%>

	<ui:input-text name="filter" styleClass="ml1" value="${form.param['filter']}" placeholder="${l.l('Filter')}" size="40"
		title="${l.l('По наименованию, комментарию, конфигурации')}"
		onSelect="$$.ajax.loadContent(this)"/>

	<ui:page-control/>
</html:form>

<c:if test="${not empty form.param.directoryId}">
	<table class="data mt1">
		<tr>
			<td width="30">&#160;</td>
			<td width="30">ID</td>
			<td width="50">${l.l('Type')}</td>
			<td width="50">${l.l('Порядок')}</td>
			<td width="35%">${l.l('Название')}</td>
			<td width="20%">${l.l('Комментарий')}</td>
			<td width="35%">${l.l('Конфигурация')}</td>
			<c:if test="${form.param.directoryId eq 'processParameter'}">
				<td width="0">&nbsp;</td>
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

				<c:url var="deleteUrl" value="/admin/directory.do">
					<c:param name="action" value="parameterDelete" />
					<c:param name="id" value="${item.id}" />
				</c:url>

				<td nowrap="nowrap">
					<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.loadContent('${editUrl}', this)"/>
					<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${deleteUrl}', {control: this}).done(() => $$.ajax.loadContent('${form.requestUrl}', this))"/>
				</td>

				<td align="right">${item.id}</td>
				<td>${item.type}</td>
				<td>${item.order}</td>
				<td>${u.escapeXml( item.title )}</td>
				<td>${u.escapeXml( item.comment )}</td>
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

							<button type="button" class="btn-grey btn-small icon" title="${l.l('Где используется')}"
									onclick="$(this).hide(); $$.ajax.load('${showUrl}', $('#${showId}'));"><i class="ti-search"></i></button>

							<div id="${showId}">
							</div>
						</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
</c:if>

<shell:state/>