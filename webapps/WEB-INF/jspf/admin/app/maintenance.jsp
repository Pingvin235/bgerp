<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}" class="center1020">
	<p:check action="org.bgerp.action.admin.AppAction:maintenanceStart">
		<form action="${form.httpRequestURI}">
			<c:set var="maintenance" value="${frd.maintenance}"/>
			<h2>${l.l('Maintenance')}</h2>
			<c:set var="command">$$.ajax.post(this).done(() => $$.ajax.load('${form.requestUrl}', $(document.getElementById('${uiid}').parentElement)))</c:set>
			<c:choose>
				<c:when test="${empty maintenance}">
					<input type="hidden" name="action" value="maintenanceStart"/>
					<textarea name="message" class="w100p" rows="4" style="resize: none;">${l.l('maintenance.message.default')}</textarea>
					<div class="mt1">
						<ui:combo-single hiddenName="delayMinutes" prefixText="${l.l('Delay in minutes before user\\\'s logoff')}:" value="2">
							<jsp:attribute name="valuesHtml">
								<li value="5">5</li>
								<li value="4">4</li>
								<li value="3">3</li>
								<li value="2">2</li>
								<li value="1">1</li>
								<li value="0">0</li>
							</jsp:attribute>
						</ui:combo-single>
						<button type="button" class="btn-grey ml1" onclick="
							if (!confirm('${l.l('Maintenance start?')}')) return;
							${command};
						">${l.l('Start')}</button>
					</div>
				</c:when>
				<c:otherwise>
					<input type="hidden" name="action" value="maintenanceCancel"/>
					<div>${frd.maintenanceState}</div>
					<div class="mt1">
						<button type="button" class="btn-grey" onclick="
							if (!confirm('${l.l('Maintenance cancel?')}')) return;
							${command};
						">${l.l('Cancel')}</button>
					</div>
				</c:otherwise>
			</c:choose>
		</form>
	</p:check>

	<c:set var="logged" value="${frd.logged}"/>

	<h2>${l.l('Logged in users')} [${logged.size()}]</h2>
	<table class="data hl">
		<tr>
			<td>${l.l('User')}</td>
			<td>${l.l('Время входа')}</td>
			<td>${l.l('Последняя активность')}</td>
			<td>IP</td>
		</tr>
		<c:forEach var="item" items="${logged}">
			<c:set var="sessions" value="${item.value}"/>
			<c:forEach var="session" items="${sessions}">
				<tr>
					<td><ui:user-link id="${item.key.id}"/></td>
					<td>${tu.format(session.loginTime, 'ymdhms')}</td>
					<td>${tu.format(session.lastActiveTime, 'ymdhms')}</td>
					<td>${session.ip}</td>
				</tr>
			</c:forEach>
		</c:forEach>
	</table>
</div>

<shell:title text="${l.l('Maintenance')}"/>
<shell:state/>