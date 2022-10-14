<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="task" value="${form.response.data.task}"/>
<c:set var="slotList" value="${form.response.data.slotList}"/>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="tt">Текущее время: <b>${tu.format(task.time, 'ymdhm')}</b></div>

<html:form action="${form.httpRequestURI}" styleId="${uiid}" styleClass="mt1">
	<input type="hidden" name="action" value="processTime"/>
	<input type="hidden" name="processId" value="${form.param.processId}"/>

	${l.l('Вывести на дату')}:
	<ui:date-time type="ymd" paramName="date" value="${form.param.date}"/>

	<button type="button" class="btn-grey ml05" onclick="$$.ajax.load(this, $(this.form).parent())">${l.l('Вывести')}</button>

	<c:url var="url" value="${form.httpRequestURI}">
		<c:param name="action" value="processTimeSet"/>
		<c:param name="processId" value="${form.param.processId}"/>
	</c:url>
	<button class="btn-grey ml2" type="button" title="Очистить занятое время" onclick="
		if (!confirm('Очистить время?')) return;
		$$.ajax.post('${url}', {control: this}).done(() => {
			$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
		})
	">${l.l('Очистить')}</button>
</html:form>

<c:if test="${not empty slotList}">
	<table class="data mt1 hl">
		<tr>
			<td>${l.l('Время')}</td>
			<td width="50%">${l.l('Group')}</td>
			<td width="50%">${l.l('Users')}</td>
			<td>&nbsp;</td>
		</tr>
		<c:forEach var="slot" items="${slotList}">
			<tr>
				<td nowrap="nowrap">${tu.format(slot.time, 'ymdhm')}</td>
				<c:url var="url" value="${form.httpRequestURI}">
					<c:param name="action" value="processTimeSet"/>
					<c:param name="processId" value="${form.param.processId}"/>
					<c:param name="time" value="${tu.format(slot.time, 'ymdhm')}"/>
					<c:param name="userIds" value="${u.toString(slot.shiftData.userIds)}"/>
				</c:url>
				<td>${ctxUserGroupMap[slot.groupId]}</td>
				<td>${u.getObjectTitles(u.getObjectList(ctxUserList, slot.shiftData.userIds))}</td>
				<td><button class="btn-grey btn-small" onclick="
					$$.ajax.post('${url}', {control: this}).done(() => {
						$$.ajax.load('${form.requestUrl}', $('#${uiid}').parent());
					})
				">Занять</button></td>
			</tr>
		</c:forEach>
	</table>
</c:if>