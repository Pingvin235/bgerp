<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="message" value="${frd.message}" scope="request"/>
<c:set var="messageType" value="${ctxSetup.getConfig('MessageTypeConfig').typeMap[message.typeId]}" scope="request"/>

<c:set var="uiid" value="${u:uiid()}"/>

<c:if test="${not empty form.returnUrl}">
	<c:set var="stateUiid" value="${u:uiid()}"/>
	<div id="${stateUiid}">
		<h1 style="display: inline-block;"><a href="#" onclick="$$.ajax.loadContent('${form.requestUrl}', $('#${uiid}')); return false;">${l.l('Обработка')}</a></h1>
		<ui:button type="back" styleClass="ml1" onclick="$$.ajax.loadContent('${form.returnUrl}', $('#${uiid}'));"/>
	</div>
	<shell:state moveSelector="#${stateUiid}"/>
</c:if>

<%-- tabs with links, e.g. BGBilling contracts --%>
<c:set var="searchTabsUiid" value="${u:uiid()}" scope="request"/>

<div id="${uiid}" class="in-inline-block in-va-top">
	<div style="width: 50%;">
		<c:set var="customer" value="${frd.customer}"/>
		<c:if test="${empty customer}">
			<html:form action="/user/message" onsubmit="this.out.click(); return false;" styleClass="in-inline-block mb1" styleId="${searchBlockId}">
				<%-- div around to eliminate in-inline-block class --%>
				<div><input type="submit" hidden/></div><%--
			--%><html:hidden property="typeId"/>
				<html:hidden property="messageId"/>
				<html:hidden property="returnUrl"/>

				<ui:combo-single name="searchId" value="${form.param.searchId}" prefixText="${l.l('Поиск')}:" styleClass="mr1"
					onSelect="$(this.form).find('>.filter').hide(); $(this.form).find('>.filter#' + this.value).show();">
					<jsp:attribute name="valuesHtml">
						<c:forEach var="item" items="${messageType.searchMap}">
							<li value="${item.key}">${item.value.title}</li>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>

				<c:forEach var="item" items="${messageType.searchMap}">
					<c:if test="${not empty item.value.jsp}">
						<div class="filter mr1" id="${item.key}" style="${form.param.searchId eq item.key ? '' : 'display: none;'}">
							<jsp:include page="${item.value.jsp}"/>
						</div>
					</c:if>
				</c:forEach>

				<button type="button" name="out" class="btn-grey" onclick="$$.ajax.load(this, $('#${uiid}').parent());">${l.l('Искать')}</button>
			</html:form>
		</c:if>

		<c:choose>
			<c:when test="${empty message.process}">
				<c:set var="processTabsUiid" value="${u:uiid()}"/>
				<div id="${processTabsUiid}">
					<ul>
						<li><a href="#process-create">${l.l('New Process')}</a></li><%--
					--%><li><a href="#process-exists">${l.l('Use Existing')}<span class='iface-state'></span></a></li>
					</ul>
					<div id="process-create">
						<form action="/user/message.do" onsubmit="return false;">
							<input type="hidden" name="method" value="processCreate"/>
							<input type="hidden" name="wizard" value="0"/>
							<input type="hidden" name="messageTypeId" value="${form.param.typeId}"/>
							<input type="hidden" name="messageId" value="${form.param.messageId}"/>

							<h2>${l.l('Links')}</h2>
							<div>
								<%@ include file="message_search_result.jsp"%>
							</div>

							<div id="typeTree">
								<jsp:include page="/WEB-INF/jspf/user/process/tree/process_type_tree.jsp"/>
							</div>

							<div class="mt1">
								<h2>${l.l('Description')}</h2>

								<textarea name="description" rows="5" style="width: 100%; resize: vertical;">${message.subject}</textarea>
								<div class="hint">${l.l('Краткое описание процесса')}</div>

								<div class="mt1">
									<c:if test="${empty customer}">
										<%@ include file="process_set_extra_actions.jsp"%>
									</c:if>
									<% out.flush(); %>

									<%-- TODO: Make button disabled <p:check action="/user/message:processCreate"> --%>
									<button class="btn-grey" type="button" onclick="
										$$.ajax.post(this).done((result) => {
											const url = '/user/message.do?id=' + result.data.id + '&returnUrl=' + encodeURIComponent('${form.returnUrl}');
											$$.ajax.load(url, $('#${uiid}').parent());
											// TODO: Reload unprocessed messages list.
										});">${l.l('Создать процесс')}</button>
								</div>
							</div>
						</form>
					</div>
					<div id="process-exists">
						<%-- TODO: Make the tab active for exiting possible processes and add count to the tab's title --%>
						<c:url var="url" value="/user/process.do">
							<c:param name="method" value="messagePossibleProcessList"/>
							<c:param name="from" value="${message.from}"/>
							<c:forEach var="item" items="${frd.searchedList}">
								<c:param name="linkObjectType" value="${item.linkObjectType}"/>
								<c:param name="linkObjectId" value="${item.linkObjectId}"/>
							</c:forEach>
							<%-- parameters for updating processes --%>
							<c:param name="messageTypeId" value="${form.param.typeId}"/>
							<c:param name="messageId" value="${form.param.messageId}"/>
							<c:param name="returnUrl" value="${form.requestUrl}"/>
							<c:param name="returnChildUiid" value="${uiid}"/>
						</c:url>
						<c:import url="${url}"/>
					</div>
				</div>

				<script>
					$("#${processTabsUiid}").tabs();
				</script>
			</c:when>
			<%-- has a process --%>
			<c:otherwise>
				<u:sc>
					<c:set var="process" value="${message.process}"/>
					<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
					<c:set var="requestUrl" value="${form.requestUrl}"/>
					<c:set var="tableId" value="${uiid}"/>

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
							<c:param name="method" value="linkList"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="objectType" value="process"/>
							<c:param name="processTypeId" value="${process.typeId}"/>
							<c:param name="header" value="${l.l('Links')}"/>
						</c:url>

						<c:remove var="form"/>
						<c:import url="${url}"/>
					</div>

					<%@ include file="/WEB-INF/jspf/user/process/process/process_parameters.jsp"%>
				</u:sc>
			</c:otherwise>
		</c:choose>

		<h2>${l.l('Set the process by ID')}</h2>

		<html:form action="/user/message" styleClass="mt1 in-table-cell" onsubmit="return false;">
			<input type="hidden" name="method" value="messageUpdateProcess"/>
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
				if (confirm('${l.l('Link the message to the process?')}')) {
					$$.ajax
						.post(this)
						.done((result) => {
							const url = '/user/message.do?id=' + result.data.id + '&returnUrl=' + encodeURIComponent('${form.returnUrl}');
							$$.ajax.load(url, $('#${uiid}').parent());
						})
				}
			</c:set>

			<%@ include file="process_set_extra_actions.jsp"%>

			<button class="btn-grey" type="button" onclick="${linkScript}">${l.l('Set')}</button>

			<div class="hint">
					${l.l('Укажите код для привязки процесса. Код "0" - для отвязки сообщения от процесса.')}
			</div>
		</html:form>
	</div><%--
--%><div style="width: 50%;" class="pl1">
		<div id="${searchTabsUiid}">
			<ul><li><a href="#message">${l.l('Message')}</a></li></ul>
			<div id="message">
				<c:set var="viewerJsp" value="${messageType.viewerJsp}"/>
				<c:choose>
					<c:when test="${not empty viewerJsp}">
						<plugin:include endpoint="${viewerJsp}"/>
					</c:when>
					<c:when test="${messageType.getClass().simpleName eq 'MessageTypeCall'}">
						<h1>${l.l('Call')}</h1>
						<div class="tt in-mt05">
							<div>${l.l('From number')}: <b>${message.from}</b></div>
							<div>${l.l('To number')}: <b>${message.to}</b></div>
							<div>${l.l('Start Time')}: <b>${tu.format(message.fromTime, 'ymdhms')}</b></div>
						</div>
					</c:when>
				</c:choose>
			</div>
		</div>
		<script>
			$("#${searchTabsUiid}").tabs();
		</script>
	</div>
</div>


