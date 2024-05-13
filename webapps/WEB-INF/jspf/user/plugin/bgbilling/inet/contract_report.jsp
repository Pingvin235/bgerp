<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/bgbilling/proto/inet.do" styleId="${uiid}">
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

	<c:set var="sendForm">$$.ajax.load($('#${uiid}'), $('#${uiid}').parent());</c:set>

	<ui:button type="out" styleClass="ml1" onclick="${sendForm}"/>

	<ui:page-control nextCommand=";${sendForm}"/>

	<script>
		$(function () {
			${onSelect}
		})
	</script>
</html:form>

<c:set var="closeAllowed" value="${ctxUser.checkPerm('ru.bgcrm.plugin.bgbilling.proto.struts.action.InetAction:connectionClose')}"/>
<c:set var="finishAllowed" value="${ctxUser.checkPerm('ru.bgcrm.plugin.bgbilling.proto.struts.action.InetAction:connectionFinish')}"/>

<c:if test="${not empty frd.list}">
	<table class="data mt1">
		<tr>
			<td></td>
			<td>ConID</td>
			<td>SessID</td>
			<td>Устройство</td>
			<td>Идентификатор</td>
			<td>Сервис</td>
			<td>С ном./на ном.</td>
			<td>IP</td>
			<td>Начало</td>
			<td>Окончание</td>
			<td>Активность</td>
			<td>Стоимость</td>
			<td>Статус</td>
			<td>Состояние</td>
		</tr>
		<c:forEach var="item" items="${frd.list}">
			<tr>
				<td>
					<c:if test="${closeAllowed or finishAllowed}">
						<u:sc>
							<c:set var="menuUiid" value="${u:uiid()}"/>
							<ui:popup-menu id="${menuUiid}">
								<c:if test="${closeAllowed}">
									<c:url var="url" value="/user/plugin/bgbilling/proto/inet.do">
										<c:param name="action" value="connectionClose"/>
										<c:param name="billingId" value="${form.param.billingId}"/>
										<c:param name="moduleId" value="${form.param.moduleId}"/>
										<c:param name="contractId" value="${form.param.contractId}"/>
										<c:param name="connectionId" value="${item.conId}"/>
									</c:url>
									<li>
										<a href="#" onclick="$$.ajax.post('${url}')" title="Послать PoD, ограничить доступ через CoA, сбросить по SNMP и т.п., в зависимости от обработчика активации сервиса устройства.">
											<img src="/img/fugue/plug-disconnect.png"/>
											Сбросить соединение (отключить)
										</a>
									</li>
								</c:if>
								<c:if test="${finishAllowed}">
									<c:url var="url" value="/user/plugin/bgbilling/proto/inet.do">
										<c:param name="action" value="connectionFinish"/>
										<c:param name="billingId" value="${form.param.billingId}"/>
										<c:param name="moduleId" value="${form.param.moduleId}"/>
										<c:param name="contractId" value="${form.param.contractId}"/>
										<c:param name="connectionId" value="${item.conId}"/>
									</c:url>
									<li>
										<a href="#" onclick="$$.ajax.post('${url}')" title="Завершение соединения в БД, как если бы вышел лимит ожидания RADIUS- или Netflow-пакета (connection.close.timeout).">
											<img src="/img/fugue/plug--minus.png"/>
											Завершить (зависшее) соединение
										</a>
									</li>
								</c:if>
							</ui:popup-menu>

							<ui:button type="more" styleClass="btn-small" onclick="$$.ui.menuInit($(this), $('#${menuUiid}'), 'left', true);"/>
						</u:sc>
					</c:if>
				</td>
				<td>${item.conId}</td>
				<td>${item.id}</td>
				<td>${item.deviceTitle}</td>
				<td>${item.acctSessId}</td>
				<td>${item.serviceTitle}</td>
				<td>${item.fromNumberToNumberAsString}</td>
				<td>${item.ip}</td>
				<td>${item.sessionStartAsString}</td>
				<td>${item.sessionStopAsString}</td>
				<td>${item.sessionActivityAsString}</td>
				<td>${item.cost}</td>
				<td>${item.statusName}</td>
				<td>${item.devStateTitle}</td>
			</tr>
		</c:forEach>
	</table>
</c:if>