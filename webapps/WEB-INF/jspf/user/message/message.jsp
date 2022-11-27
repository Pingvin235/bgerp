<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="message" value="${form.response.data.message}" scope="request"/>
<c:set var="form" value="${form}"/>

<c:set var="messageType" value="${ctxSetup.getConfig('ru.bgcrm.dao.message.config.MessageTypeConfig').typeMap[message.typeId]}" scope="request"/>

<c:set var="uiid" value="${u:uiid()}"/>

<c:if test="${not empty form.returnUrl}">
	<c:set var="stateUiid" value="${u:uiid()}"/>
	<div id="${stateUiid}">
		<h1 style="display: inline-block;"><a href="#" onclick="$$.ajax.loadContent('${form.requestUrl}', $('#${uiid}')); return false;">${l.l('Обработка')}</a></h1>
		<ui:button type="back" styleClass="ml1" onclick="$$.ajax.loadContent('${form.returnUrl}', $('#${uiid}'));"/>
	</div>
	<shell:state moveSelector="#${stateUiid}"/>
</c:if>

<%-- добавляются табы с найденными объектами, в т.ч. договорами биллинга --%>
<c:set var="searchTabsUiid" value="${u:uiid()}" scope="request"/>

<div id="${uiid}" class="in-inline-block in-va-top">
	<div style="width: 50%;">
		<c:choose>
			<%-- процесс ещё не привязан --%>
			<c:when test="${empty message.process}">
				<h2>${l.l('Создать новый процесс')}</h2>

				<form action="/user/message.do" onsubmit="return false;">
					<input type="hidden" name="action" value="processCreate"/>
					<input type="hidden" name="wizard" value="0"/>
					<input type="hidden" name="messageTypeId" value="${form.param.typeId}"/>
					<input type="hidden" name="messageId" value="${form.param.messageId}"/>

					<div id="typeTree">
						<jsp:include page="/WEB-INF/jspf/user/process/tree/process_type_tree.jsp"/>
					</div>

					<div class="mt1">
						<b>${l.l('Привязать')}:</b><br/>

						<c:set var="searchBlockId" value="${u:uiid()}"/>
						<c:set var="searchResultId" value="${u:uiid()}"/>

						<div class="in-inline-block" id="${searchBlockId}">
							<u:sc>
								<c:set var="valuesHtml">
									<c:forEach var="item" items="${messageType.searchMap}">
										<li value="${item.key}">${item.value.title}</li>
									</c:forEach>
								</c:set>
								<c:set var="hiddenName" value="searchId"/>
								<c:set var="value" value="${form.param.searchId}"/>
								<c:set var="prefixText" value="${l.l('Поиск')}:"/>
								<c:set var="styleClass" value="mr1"/>
								<c:set var="onSelect">$('#${searchBlockId} > .filter').hide();$('#${searchBlockId} > .filter#' + $hidden.val() ).show();</c:set>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
							</u:sc>

							<c:set var="searchScript">
								const searchId = this.form.searchId.value;
								const url = '/user/message.do?typeId=${form.param.typeId}&messageId=' + encodeURIComponent('${form.param.messageId}') +
									'&searchId=' + searchId + '&returnUrl=' + encodeURIComponent('${form.returnUrl}') +
									'&' + $(this.form).find('.filter#' + searchId).serializeAnything();
								$$.ajax.load(url, $('#${uiid}').parent(), {control: this});
							</c:set>

							<c:set var="searchOnEnter" scope="request">onkeypress="if (enterPressed(event)) {${searchScript}; return false;}"</c:set>

							<c:forEach var="item" items="${messageType.searchMap}">
								<c:if test="${not empty item.value.jsp}">
									<div class="filter mr1" id="${item.key}" style="${form.param.searchId eq item.key ? '' : 'display: none;'}">
										<jsp:include page="${item.value.jsp}"/>
									</div>
								</c:if>
							</c:forEach>

							<button type="button" class="btn-grey" onclick="${searchScript}">${l.l('Искать')}</button>
						</div>

						<div id="${searchResultId}">
							<%@ include file="message_search_result.jsp"%>
						</div>
					</div>

					<div class="mt1">
						<b>${l.l('Description')}:</b><br/>

						<textarea name="description" rows="5" style="width: 100%; resize: vertical;">${message.subject}</textarea>
						<div class="hint">${l.l('Краткое описание процесса')}</div>

						<div class="mt1">
							<%@ include file="process_link_params.jsp"%>

							<%-- TODO: Make button disabled <p:check action="ru.bgcrm.struts.action.MessageAction:processCreate"> --%>
							<button class="btn-grey" type="button" onclick="
								$$.ajax.post(this.form, {control: this}).done((result) => {
									const url = '/user/message.do?id=' + result.data.id + '&returnUrl=' + encodeURIComponent('${form.returnUrl}');
									$$.ajax.load(url, $('#${uiid}').parent());
									// TODO: Reload unprocessed messages list.
								});">${l.l('Создать процесс')}</button>
						</div>
					</div>
				</form>

				<h2>${l.l('Возможные процессы')}</h2>

				<div>
					<c:url var="url" value="/user/process.do">
						<c:param name="action" value="messageRelatedProcessList"/>
						<c:param name="from" value="${message.from}"/>
						<c:forEach var="item" items="${form.response.data.searchedList}">
							<c:param name="object" value="${item.linkedObjectType}:${item.linkedObjectId}"/>
						</c:forEach>
						<%-- parameters for updating processes --%>
						<c:param name="messageTypeId" value="${form.param.typeId}"/>
						<c:param name="messageId" value="${form.param.messageId}"/>
						<c:param name="returnUrl" value="${form.requestUrl}"/>
						<c:param name="returnChildUiid" value="${uiid}"/>
					</c:url>
					<c:import url="${url}"/>
				</div>
			</c:when>
			<%-- has a process --%>
			<c:otherwise>
				<u:sc>
					<c:set var="process" value="${message.process}"/>
					<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
					<c:set var="requestUrl" value="${form.requestUrl}"/>
					<c:set var="tableId" value="${uiid}"/>

					<h2>${l.l('ПРОЦЕСС')}</h2>

					<table style="width: 100%;" class="oddeven">
						<%@ include file="/WEB-INF/jspf/user/process/process/process_header.jsp"%>
						<tr valign="top">
							<td>
								<%@ include file="/WEB-INF/jspf/user/process/process/process_description.jsp"%>
							</td>
						</tr>
						<tr valign="top">
							<td>
								<%@ include file="/WEB-INF/jspf/user/process/process/process_executors.jsp"%>
							</td>
						</tr>
					</table>

					<div>
						<c:url var="url" value="/user/link.do">
							<c:param name="action" value="linkList"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="objectType" value="process"/>
							<c:param name="processTypeId" value="${process.typeId}"/>
							<c:param name="header" value="${l.l('Привязки')}"/>
						</c:url>

						<c:remove var="form"/>
						<c:import url="${url}"/>
					</div>

					<%@ include file="/WEB-INF/jspf/user/process/process/process_parameters.jsp"%>
				</u:sc>
			</c:otherwise>
		</c:choose>

		<h2>${l.l('Привязать процесс')}</h2>

		<html:form action="/user/message" styleClass="mt1 in-table-cell" onsubmit="return false;">
			<input type="hidden" name="action" value="messageUpdateProcess"/>
			<c:choose>
				<c:when test="${message.id gt 0}">
					<input type="hidden" name="id" value="${message.id}"/>
				</c:when>
				<c:otherwise>
					<input type="hidden" name="messageTypeId" value="${form.param.typeId}"/>
					<html:hidden property="messageId"/>
				</c:otherwise>
			</c:choose>

			<input type="text" size="3" name="processId" class="mr1 text-center"/>

			<c:set var="linkScript">
				if (confirm('${l.l('Привязать сообщение к указанному процессу?')}')) {
					$$.ajax
						.post(this)
						.done((result) => {
							const url = '/user/message.do?id=' + result.data.id + '&returnUrl=' + encodeURIComponent('${form.returnUrl}');
							$$.ajax.load(url, $('#${uiid}').parent());
						})
				}
			</c:set>

			<%@ include file="process_link_params.jsp"%>

			<button class="btn-grey" type="button" onclick="${linkScript}">${l.l('Привязать')}</button>

			<div class="hint">
					${l.l('Укажите код для привязки процесса. Код "0" - для отвязки сообщения от процесса.')}
			</div>
		</html:form>
	</div><%--
--%><div style="width: 50%;" class="pl1">
		<c:set var="viewerJsp" value="${messageType.viewerJsp}"/>
		<c:set var="typeCall" value="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeCall'}"/>
		<c:choose>
			<c:when test="${not empty viewerJsp}">
				<plugin:include endpoint="${viewerJsp}"/>
			</c:when>
			<c:when test="${typeCall}">
				<h2>${l.l('Звонок')}</h2>
				<div class="tt in-mt05">
					<div>${l.l('С номера')}: <b>${message.from}</b></div>
					<div>${l.l('На номер')}: <b>${message.to}</b></div>
					<div>${l.l('Время начала')}: <b>${tu.format(message.fromTime, 'ymdhms')}</b></div>
				</div>
			</c:when>
		</c:choose>
	</div>
</div>

<div id="${searchTabsUiid}">
	<ul></ul>
</div>

