<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="task" value="${form.response.data.task}"/>
<c:set var="slotList" value="${form.response.data.slotList}"/>
<%-- <c:set var="timeSet" value="${form.response.data.timeSet}"/> --%>

<c:set var="uiid" value="${u:uiid()}"/>

<div class="tt">Текущее время: <b>${tu.format( task.time, 'ymdhm' )}</b></div>

<html:form action="/user/plugin/callboard/work" styleId="${uiid}" styleClass="mt1">
	<input type="hidden" name="action" value="processTime"/>
	<input type="hidden" name="processId" value="${form.param.processId}"/>

	${l.l('Вывести на дату')}:
	<u:sc>
		<c:set var="type" value="ymd"/>
		<c:set var="uiid" value="${u:uiid()}"/>
		<input type="text" id="${uiid}" name="date" value="${form.param.date}"/>
		<c:set var="selector" value="#${uiid}"/>
		<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
	</u:sc>

	<button type="button" class="btn-grey ml05" onclick="openUrlToParent( formUrl( this.form ), $(this.form) )">${l.l('Вывести')}</button>

	<c:url var="url" value="/user/plugin/callboard/work.do">
		<c:param name="action" value="processTimeSet"/>
		<c:param name="processId" value="${form.param.processId}"/>
	</c:url>
	<button class="btn-grey ml2" type="button" title="Очистить занятое время" onclick="if( confirm( 'Очистить время?' ) && sendAJAXCommand( '${url}' ) ){ openUrlToParent( '${form.requestUrl }', $('#${uiid}') ) }">${l.l('Очистить')}</button>
</html:form>

<c:if test="${not empty slotList}">
	<table class="data mt1" style="width: 100%;">
		<tr>
			<td width="100%">Время</td>
			<td>&nbsp;</td>
		</tr>
		<c:forEach var="slot" items="${slotList}">
			<tr>
				<td>${tu.format( slot.time, 'ymdhm' )}</td>
				<c:url var="url" value="/user/plugin/callboard/work.do">
					<c:param name="action" value="processTimeSet"/>
					<c:param name="processId" value="${form.param.processId}"/>
					<c:param name="time" value="${tu.format( slot.time, 'ymdhm' )}"/>
				</c:url>
				<td><button class="btn-white btn-small" onclick="if( sendAJAXCommand( '${url}' ) ){ openUrlToParent( '${form.requestUrl }', $('#${uiid}') ) }">Занять</button></td>
			</tr>
		</c:forEach>
	</table>
</c:if>