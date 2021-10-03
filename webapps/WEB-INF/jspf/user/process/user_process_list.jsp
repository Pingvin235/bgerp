<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="user/process" styleClass="mb05" styleId="${uiid}">
	<input type="hidden" name="action" value="userProcessList"/>

	<div class="tableIndent in-mb05-all">
		<ui:date-time
			paramName="createDate" value="${form.param.createDate}"
			placeholder="${l.l('Дата создания')}"
			styleClass="mr1" editable="true"/>

		<ui:date-time
			paramName="closeDate" value="${form.param.closeDate}"
			placeholder="${l.l('Дата закрытия')}"
			styleClass="mr1" editable="true"/>

		<ui:combo-single
			hiddenName="open" value="${form.param.open}" prefixText="${l.l('Закрыт')}:"
			styleClass="mr1" widthTextValue="100px"
			onSelect="const $form = $('#${uiid}'); $$.ajax.load($form, $form.parent());">
			<jsp:attribute name="valuesHtml">
				<li value="1">${l.l('Открытые')}</li>
				<li value="0">${l.l('Закрытые')}</li>
				<li value="">${l.l('Все')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<ui:page-control nextCommand="; const $form = $('#${uiid}'); $$.ajax.load($form, $form.parent());"/>
	</div>
</html:form>

<c:set var="uiid" value="${u:uiid()}"/>

<%@ include file="/WEB-INF/jspf/table_row_edit_mode.jsp"%>

<table class="data" class="center1020" id="${uiid}">
	<tr>
		<td>ID</td>
		<td>${l.l('Время создания')}</td>
		<td>${l.l('Время закрытия')}</td>
		<td>${l.l('Тип')}</td>
		<td>${l.l('Статус')}</td>
		<td>${l.l('Описание')}</td>
	</tr>
	<c:forEach var="process" items="${form.response.data.list}">
		<tr openCommand="openProcess(${process.id })">
			<td nowrap="nowrap"><a href="#" onclick="openProcess(${process.id}); return false;">${process.id}</a></td>
			<td nowrap="nowrap">${u:formatDate( process.createTime, 'ymdhms' )}</td>
			<td nowrap="nowrap">${u:formatDate( process.closeTime, 'ymdhms' )}</td>
			<td>${ctxProcessTypeMap[process.typeId].title}</td>
			<td>${ctxProcessStatusMap[process.statusId].title}</td>
			<td width="100%">
				<%@ include file="/WEB-INF/jspf/user/process/reference.jsp"%>
			</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="${l.l('Мои процессы')}"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
