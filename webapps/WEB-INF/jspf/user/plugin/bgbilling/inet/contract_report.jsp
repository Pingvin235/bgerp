<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/bgbilling/proto/inet" styleId="${uiid}">
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>

	<c:set var="onSelect">$('#${uiid} #dateFilter').toggle( $('#${uiid}')[0].action.value == 'sessionLogContractList' )</c:set>

	<ui:combo-single hiddenName="action" value="${form.param.action}" prefixText="Тип:" onSelect="${onSelect}">
		<jsp:attribute name="valuesHtml">
			<li value="sessionAliveContractList">Активные</li>
			<li value="sessionLogContractList">История</li>
		</jsp:attribute>
	</ui:combo-single>

	<div id="dateFilter" style="display: inline-block;" class="pl05">
		<ui:date-month-days/>
	</div>

	<c:set var="sendForm">openUrlToParent( formUrl( $('#${uiid}') ), $('#${uiid}') );</c:set>

	<button type="button" class="ml2 btn-grey" onclick="${sendForm}" title="${l.l('Вывести')}">=&gt;</button>

	<ui:page-control nextCommand=";${sendForm}"/>

	<script>
		$(function()
		{
			${onSelect}
		})
	</script>
</html:form>

<c:if test="${not empty form.response.data.list}">
	<table class="data mt1" style="width: 100%;">
		<tr>
			<td>ConID</td>
			<td>SessID</td>
			<td>Устройство</td>
			<td>Идентификатор</td>
			<td>Сервис</td>
			<td>Реалм</td>
			<td>С ном./на ном.</td>
			<td>IP</td>
			<td>Начало</td>
			<td>Окончание</td>
			<td>Активность</td>
			<td>Стоимость</td>
			<td>Статус</td>
			<td>Состояние</td>
		</tr>
		<c:forEach var="item" items="${form.response.data.list}">
			<tr>
				<td>${item.conId}</td>
				<td>${item.id}</td>
				<td>${item.deviceId}</td>
				<td>${item.acctSessId}</td>
				<td>${item.serviceTitle}</td>
				<td>${item.realm}</td>
				<td>${item.fromNumberToNumberAsString}</td>
				<td>${item.inetAddress}</td>
				<td>${item.sessionStartAsString}</td>
				<td>${item.sessionStopAsString}</td>
				<td>${item.sessionActivityAsString}</td>
				<td>${item.sessionCost}</td>
				<td>${item.statusName}</td>
				<td>${item.devState eq 1 ? 'подключено' : 'отключено'}</td>
			</tr>
		</c:forEach>
	</table>
</c:if>