<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="baseUrl" value="${form.requestURI}">
	<c:param name="boardId" value="${form.param.boardId}"/>
	<c:param name="id" value="${form.id}"/>
</c:url>

<c:set var="uiid" value="${u:uiid()}"/>
<span id="${uiid}"/>
<c:set var="reloadScript">.done(() => $$.ajax.load('${form.requestUrl}', document.getElementById('${uiid}').parentElement)</c:set>

<c:set var="slot" value="${frd.slot}"/>
<c:choose>
	<c:when test="${not empty slot}">
		<span class="tt">
			${l.l('Date')}:
			<b>${tu.format(slot.cell.row.date, 'ymd')}</b>
		</span>

		<c:url var="url" value="${baseUrl}">
			<c:param name="method" value="slotFree"/>
		</c:url>
		<ui:button type="del" styleClass="btn-small mr1" onclick="
			$$.ajax.post('${url}')${reloadScript})
		"/>

		<c:choose>
			<c:when test="${empty slot.time}">
				<table class="data hl mt1">
					<tr>
						<td class="min"></td>
						<td>${l.l('Time')}</td>
					</tr>

					<c:url var="setUrl" value="${baseUrl}">
						<c:param name="method" value="slotSetTime"/>
					</c:url>

					<c:forEach var="time" items="${frd.times}">
						<tr>
							<td>
								<c:url var="url" value="${setUrl}">
									<c:param name="time" value="${time}"/>
								</c:url>
								<button onclick="
									$$.ajax.post('${url}')${reloadScript});
								" class="btn-white btn-small icon"><i class="ti-check"></i></button>
							</td>
							<td>${time}</td>
						</tr>
					</c:forEach>
				</table>
			</c:when>
			<c:otherwise>
				<span class="tt">
					${l.l('Time')}:
					<b>${slot.formattedTime}</b>
				</span>

				<c:url var="url" value="${baseUrl}">
					<c:param name="method" value="slotFreeTime"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small mr1" onclick="
					$$.ajax.post('${url}')${reloadScript})
				"/>
			</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		<span class="tt">${l.l('The process requires time')}: <b>${tu.format(frd.duration)}</b></span>
		<table class="data hl mt1">
			<tr>
				<td class="min"></td>
				<td class="min">${l.l('Date')}</td>
				<td>${l.l('Group')}</td>
				<td>${l.l('Available')}</td>
			</tr>

			<c:url var="setUrl" value="${baseUrl}">
				<c:param name="method" value="slotSet"/>
			</c:url>

			<c:forEach var="me" items="${frd.days.entrySet()}">
				<tr>
					<td>
						<c:url var="url" value="${setUrl}">
							<c:param name="date" value="${tu.format(me.key, 'ymd')}"/>
							<c:param name="groupId" value="${me.value.first.id}"/>
						</c:url>
						<button onclick="
							$$.ajax.post('${url}')${reloadScript});
						" class="btn-white btn-small icon"><i class="ti-check"></i></button>
					</td>
					<td>${tu.format(me.key, 'ymd')}</td>
					<td>${me.value.first.title}</td>
					<td>${tu.format(me.value.second)}</td>
				</tr>
			</c:forEach>
		</table>
	</c:otherwise>
</c:choose>
