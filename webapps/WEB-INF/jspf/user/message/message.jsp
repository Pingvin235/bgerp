<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="message" value="${form.response.data.message}" scope="request"/>
<c:set var="form" value="${form}"/>

<c:set var="config" value="${u:getConfig( ctxSetup, 'ru.bgcrm.dao.message.config.MessageTypeConfig' ) }"/>
<c:set var="messageType" value="${config.typeMap[message.typeId]}" scope="request"/>

<c:set var="typeEmail" value="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeEmail'}"/>
<c:set var="typeCall" value="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeCall'}"/>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}" class="in-ml1">
	<h1 style="display: inline-block;"><a href="#UNDEF" onclick="openUrlContent('${form.requestUrl}'); return false;">Обработка</a></h1>
	<button  type="button" class="ml1 btn-white" onClick="openUrlContent('${form.returnUrl}')" title="Закрыть">&lt;</button>
</div>

<script>
	$(function()
	{
		var $state = $('#title > .status:visible > .wrap > .center');
		$state.html( "" );

		$('#${uiid}').appendTo( $state );
	})
</script>

<c:set var="uiid" value="${u:uiid()}"/>
<%-- добавляются табы с найденными объектами, в т.ч. договорами биллинга --%>
<c:set var="searchTabsUiid" value="${u:uiid()}" scope="request"/>

<div style="width: 100%;" id="${uiid}" class="in-inline-block in-va-top">
	<div style="width: 50%;">
		<c:choose>
			<%-- процесс ещё не привязан --%>
			<c:when test="${empty message.process}">
				<h2>Создать новый процесс</h2>

				<form action="/user/process.do" style="width:100%;" onsubmit="return false;">
					<input type="hidden" name="action" value="processCreate"/>
					<input type="hidden" name="trans_id" value="${uiid}"/>
					<input type="hidden" name="wizard" value="0"/>
					<input type="hidden" name="messageId" value="${message.id}"/>

					<div id="typeTree">
						<jsp:include page="/WEB-INF/jspf/user/process/tree/process_type_tree.jsp"/>
					</div>

					<c:set var="customerLinkRoleConfig" value="${u:getConfig( setup, 'ru.bgcrm.model.customer.config.ProcessLinkModesConfig' )}"/>

					<div class="mt1">
						<b>Привязать:</b><br/>

						<c:set var="searchBlockId" value="${u:uiid()}"/>

						<div class="in-inline-block" id="${searchBlockId}">
							<u:sc>
								<c:set var="valuesHtml">
									<c:forEach var="item" items="${messageType.searchMap}">
										<li value="${item.key}">${item.value.title}</li>
									</c:forEach>
								</c:set>
								<c:set var="hiddenName" value="searchId"/>
								<c:set var="value" value="${form.param.searchId}"/>
								<c:set var="prefixText" value="Поиск:"/>
								<c:set var="styleClass" value="mr1"/>
								<c:set var="onSelect">$('#${searchBlockId} > .filter').hide();$('#${searchBlockId} > .filter#' + $hidden.val() ).show();</c:set>
								<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
							</u:sc>

							<c:set var="searchScript">
								var searchId = this.form.searchId.value;
								var url = '/user/message.do?id=${form.id}&typeId=${form.param.typeId}&messageId=' + encodeURIComponent( '${form.param.messageId}' ) +
									'&searchId=' + searchId + '&returnUrl=' + encodeURIComponent( '${form.returnUrl}' ) +
									'&' + $(this.form).find( '.filter#' + searchId ).serializeAnything();
								openUrlToParent( url, $('#${uiid}') );
							</c:set>

							<c:set var="searchOnEnter" scope="request">onkeypress="if( enterPressed( event ) ){ ${searchScript}; return false;}"</c:set>

							<c:forEach var="item" items="${messageType.searchMap}">
								<c:if test="${not empty item.value.jsp}">
									<div class="filter mr1" id="${item.key}" style="${form.param.searchId eq item.key ? '' : 'display: none;'}">
										<jsp:include page="${item.value.jsp}"/>
									</div>
								</c:if>
							</c:forEach>

							<button type="button" class="btn-grey" onclick="${searchScript}">Искать</button>
						</div>

						<table class="data mt05" style="width: 100%;">
							<tr>
								<td>&nbsp;</td>
								<td>ID</td>
								<td>Тип</td>
								<td width="100%">Наименование</td>
							</tr>

							<c:forEach var="item" items="${searchedList}" varStatus="status">
								<c:set var="item" value="${item}" scope="request"/>

								<tr>
									<td>
										<input type="checkbox" name="linked" value="${item.linkedObjectType}*${item.linkedObjectId}*${fn:escapeXml( item.linkedObjectTitle )}">
									</td>
									<td>${item.linkedObjectId}</td>

									<c:set var="customerLinkRole" value="${customerLinkRoleConfig.modeMap[item.linkedObjectType]}"/>

									<c:if test="${not empty customerLinkRole}">
										<td>${customerLinkRole}</td>
										<td><a href="#UNDEF" onclick="openCustomer( ${item.linkedObjectId} ); return false;">${fn:escapeXml( item.linkedObjectTitle )}</a></td>
									</c:if>

									<c:set var="endpoint" value="user.message.search.result.jsp"/>
									<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
								</tr>
							</c:forEach>
						</table>
					</div>

					<div class="mt1">
						<b>Описание:</b><br/>

						<textarea name="description" rows="5" style="width: 100%; resize: vertical;">${message.subject}</textarea>
						<div class="hint">Краткое описание процесса</div>

						<c:set var="createCommand">
							var result = sendAJAXCommand( formUrl( this.form ) );
							if( !result )
							{
								return;
							}

							var processId = result.data.process.id;
							$( '#${uiid} input:checkbox:checked' ).each( function()
							{
								var tokens = $(this).val().split( '*' );

								var url =
									'/user/link.do?action=addLink&objectType=process&id=' + processId + '&linkedObjectType=' + tokens[0] +
									'&linkedObjectId=' + tokens[1] + '&linkedObjectTitle=' + encodeURIComponent( tokens[2] );
								if( !sendAJAXCommand( url) )
								{
									return;
								}
							});

							var url =
								'/user/message.do?action=messageUpdateProcess&id=${form.id}&processId=' + processId + '&trans_id=${uiid}&trans_end=1' +
								'&notification=' + $(this.form.notification).val() +
								'&contactSaveMode=' + $(this.form.contactSaveMode).val() +
								'&typeId=${form.param.typeId}' +
								'&messageId=' + encodeURIComponent( '${form.param.messageId}' );

							var result = sendAJAXCommand( url );
							if( result )
							{
								var url = '/user/message.do?id=' + result.data.id + '&returnUrl=${form.returnUrl}';
								openUrlToParent( url, $('#${uiid}') );
							}
						</c:set>

						<div class="mt1">
							<%@ include file="process_link_params.jsp"%>

							<button class="btn-grey" type="button" onclick="${createCommand}">Создать процесс</button>
						</div>
					</div>
				</form>

				<h2>Возможные процессы</h2>

				<div>
					<c:url var="url" value="/user/process.do">
						<c:param name="action" value="messageRelatedProcessList"/>
						<c:param name="from" value="${message.from}"/>
						<c:forEach var="item" items="${searchedList}">
							<c:param name="object" value="${item.linkedObjectType}:${item.linkedObjectId}"/>
						</c:forEach>
					</c:url>
					<c:import url="${url}"/>
				</div>
			</c:when>
			<%-- процесс привязан --%>
			<c:otherwise>
				<u:sc>
					<c:set var="process" value="${message.process}"/>
					<c:set var="processType" value="${ctxProcessTypeMap[process.typeId]}"/>
					<c:set var="requestUrl" value="${form.requestUrl}"/>
					<c:set var="tableId" value="${uiid}"/>

					<h2>ПРОЦЕСС</h2>

					<table style="width: 100%;" class="oddeven">
						<%@ include file="/WEB-INF/jspf/user/process/process/process_header.jsp"%>
						<tr>
							<c:set var="statusEditorUiid" value="${u:uiid()}"/>
							<td id="${statusEditorUiid}">
								<%@ include file="/WEB-INF/jspf/user/process/process/process_status.jsp"%>
							</td>
						</tr>
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
							<c:param name="forwardFile" value="/WEB-INF/jspf/user/process/process/link_list.jsp"/>
							<c:param name="id" value="${process.id}"/>
							<c:param name="objectType" value="process"/>
							<c:param name="processTypeId" value="${process.typeId}"/>
							<c:param name="header" value="Привязки"/>
						</c:url>

						<c:remove var="form"/>
						<c:import url="${url}"/>
					</div>

					<%@ include file="/WEB-INF/jspf/user/process/process/process_parameters.jsp"%>
				</u:sc>
			</c:otherwise>
		</c:choose>

		<h2>Привязать процесс</h2>

		<html:form action="/user/message" styleClass="mt1 in-table-cell" onsubmit="return false;">
			<input type="hidden" name="action" value="messageUpdateProcess"/>
			<c:choose>
				<c:when test="${message.id gt 0}">
					<input type="hidden" name="id" value="${message.id}"/>
				</c:when>
				<c:otherwise>
					<html:hidden property="typeId"/>
					<html:hidden property="messageId"/>
				</c:otherwise>
			</c:choose>

			<input type="text" size="3" name="processId" class="mr1 text-center"/>

			<c:set var="linkScript">
				if( confirm( 'Привязать сообщение к указанному процессу?' ) )
				{
					var result = sendAJAXCommand( formUrl( this.form ) );
					if( result )
					{
						var url = '/user/message.do?id=' + result.data.id + '&returnUrl=' + encodeURIComponent( '${form.returnUrl}' );
						openUrlToParent( url, $('#${uiid}') );
					}
				}
			</c:set>

			<%@ include file="process_link_params.jsp"%>

			<button class="btn-grey" type="button" onclick="${linkScript}">Привязать</button>

			<div class="hint">
				Укажите код для привязки процесса. Код "0" - для отвязки сообщения от процесса.
			</div>
		</html:form>

		<%--
		<button class="btn-grey mt1" type="button" onclick="openUrlContent( '${form.returnUrl}' )">Закрыть</button>
		--%>
	</div><%--
--%><div style="width: 50%;" class="pl1">
		<c:choose>
			<c:when test="${message.processId gt 0}">
				<c:url var="url" value="/user/message.do">
					<c:param name="action" value="processMessageList"/>
					<c:param name="markMessageId" value="${message.id}"/>
					<c:param name="processId" value="${message.processId}"/>
				</c:url>
				<c:import url="${url}"/>
			</c:when>

			<c:when test="${typeEmail}">
				<h2>Сообщение</h2>

				<b>Тема:</b> ${message.subject}<br/>
				<b>От:</b> <a href="mailto:${message.from}">${message.from}</a><br/>
				<b>Текст:</b><br/>
					<c:set var="text" value="${message.text}"/>
					<%@ include file="email_text_prepare.jsp"%>
					${u:htmlEncode( text )}
				<c:if test="${not empty message.attachList}">
					<br/><br/><b>Вложения (можно загрузить только после привязки процесса):</b><br/>

					<c:forEach var="item" items="${message.attachList}">
						<c:choose>
							<c:when test="${message.id gt 0}">
								<c:url var="url" value="../user/file.do">
									<c:param name="id" value="${item.id}"/>
									<c:param name="title" value="${item.title}"/>
									<c:param name="secret" value="${item.secret}"/>
								</c:url>
								<a href="${url}">${item.title}</a><br/>
							</c:when>
							<c:otherwise>
								${item.title}
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</c:if>
			</c:when>
			<c:when test="${typeCall}">
				<h2>Звонок</h2>
				<div class="tt in-mt05">
					<div>С номера: <b>${message.from}</b></div>
					<div>На номер: <b>${message.to}</b></div>
					<div>Время начала: <b>${u:formatDate( message.fromTime, 'ymdhms' )}</b></div>
				</div>
			</c:when>
		</c:choose>
	</div>
</div>

<div id="${searchTabsUiid}">
	<ul></ul>
</div>