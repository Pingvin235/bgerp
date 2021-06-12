<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uploadFormId" value="${u:uiid()}"/>
<c:set var="uploadListId" value="${u:uiid()}"/>

<c:set var="message" scope="request" value="${form.response.data.message}"/>
<c:set var="config" value="${u:getConfig( ctxSetup, 'ru.bgcrm.dao.message.config.MessageTypeConfig' ) }"/>

<form id="${uploadFormId}" action="/user/file.do" method="POST" enctype="multipart/form-data" name="form" style="position: absolute; top: -100px;">
	<input type="hidden" name="action" value="temporaryUpload"/>
	<input type="hidden" name="responseType" value="json"/>
	<input type="file" name="file" />
</form>


<script>
	$$.ajax.upload('${uploadFormId}', 'message-attach-upload', function (response) {
		const fileId = response.data.file.id;
		const fileTitle = response.data.file.title;

		const deleteCode = "$$.ajax.post('/user/file.do?action=temporaryDelete&id=" + fileId + "').done(() => {$(this.parentNode).remove()})";

		$('#${uploadListId}').append(
				"<div>" +
				"<input type=\"hidden\" name=\"tmpFileId\" value=\""+ fileId + "\"/>" +
				"<button class=\"btn-white btn-small mr1 icon\" type=\"button\" value=\"X\" onclick=\"" + deleteCode + "\"><i class=\"ti-close\"></i></button>" + fileTitle +
				"</div>"
		);
	});
</script>

<c:set var="editorUiid" value="${u:uiid()}"/>
<c:set var="typeComboUiid" value="${u:uiid()}"/>

<c:set var="typeChangedScript">
	$$.message.editorTypeChanged('${editorUiid}', '${typeComboUiid}');
</c:set>

<html:form action="/user/message" styleId="${editorUiid}" styleClass="editorStopReload">
	<input type="hidden" name="action" value="messageUpdate"/>
	<html:hidden property="processId"/>
	<html:hidden property="id"/>
	<html:hidden property="areaId"/>

	<c:if test="${not empty message}">
		<input type="hidden" id="lock-${message.lockEdit}" name="lockFree"/>
	</c:if>

	<div style="display: table; width: 100%;">
		<div class="in-table-cell">
			<div style="vertical-align: top; width: 30px;" id="typeSelectContainer">
				<h2>${l.l('Тип')}</h2>

				<c:remove var="disable"/>
				<c:choose>
					<c:when test="${not empty message}">
						<c:set var="value" value="${message.typeId}"/>
						<c:set var="disable" value="1"/>
					</c:when>
					<c:when test="${not empty form.param.messageType}">
						<c:set var="value" value="${form.param.messageType}"/>
						<c:set var="disable" value="1"/>
					</c:when>
					<c:when test="${not empty form.param.messageTypeAdd}">
						<c:set var="value" value="${form.param.messageTypeAdd}"/>
					</c:when>
				</c:choose>
				
				<c:set var="perm" value="${p:get( ctxUser.id, 'ru.bgcrm.struts.action.MessageAction:messageUpdate')}"/>
				<c:set var="allowedTypeIds" value="${u:toIntegerSet( perm['allowedTypeIds'] ) }"/>

				<ui:combo-single
					id="${typeComboUiid}" hiddenName="typeId" widthTextValue="120px"
					value="${value}" disable="${disable}" onSelect="${typeChangedScript}">
					<jsp:attribute name="valuesHtml">
						<c:forEach var="item" items="${config.typeMap}">
							<c:if test="${empty allowedTypeIds or allowedTypeIds.contains(item.key)}">
								<c:set var="messageType" value="${item.value}"/>
	
								<c:remove var="subject"/>
								<c:remove var="address"/>
								<c:remove var="attach"/>
								<c:set var="subject">
									<c:if test="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeEmail' or
												 messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeNote'}">subject='true'</c:if>
								</c:set>
								<c:set var="address">
									<c:if test="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeEmail'}">address='true'</c:if>
								</c:set>
								<c:set var="attach">
									<c:if test="${messageType.attachmentSupport}">attach='true'</c:if>
								</c:set>
	
								<%-- специальный редактор сообщения либо стандартный --%>
								<c:choose>
									<c:when test="${messageType.specialEditor}">
										<li value="${item.key}" editor="${messageType.getClass().getName()}">${item.value.title}</li>
									</c:when>
									<c:otherwise>
										<li value="${item.key}" ${subject} ${address} ${attach}>${item.value.title}</li>
									</c:otherwise>
								</c:choose>
							</c:if>
						</c:forEach>
					</jsp:attribute>
				</ui:combo-single>
			</div>
			<div id="address" class="pl1" style="width: 100%; vertical-align: top;">
				<h2>${l.l('Получатель')}</h2>
				<input type="text" name="to" style="width: 100%;" placeholder="${l.l('Для')} EMail: addr1@domain.com, addr2@domain.com; CC: copy1@domain.com, copy2.domain.com" value="${message.to}"/>
			</div>
		</div>
		<c:set var="tagConfig" value="${u:getConfig(ctxSetup, 'ru.bgcrm.model.config.TagConfig')}"/>
		<c:if test="${not empty tagConfig and not empty tagConfig.tagList}">
			<div>
				<h2>${l.l('Теги')}</h2>
				<input type="hidden" name="updateTags" value="1"/>
				<ui:select-mult list="${tagConfig.tagList}" values="${form.response.data.messageTagIds}" hiddenName="tagId"/>
			</div>
		</c:if>
		<div id="subject">
			<h2>${l.l('Тема')}</h2>
			<input type="text" name="subject" style="width: 100%;" value="${message.subject}"/>
		</div>
		<div>
			<h2>${l.l('Сообщение')}</h2>
			<textarea rows="20" style="width: 100%; resize: vertical;" name="text" class="tabsupport">${message.text}</textarea>
			<span class="hint">${l.l('Вы можете использовать #ID для ссылок на другие процессы, подобные записи будут автоматически преобразованы в ссылки открытия карточек')}.</span>
		</div>
		<div id="attach">
			<h2>${l.l('Вложения')}</h2>
			<div id="${uploadListId}" class="in-mb05-all">
				<%-- уже загруженные вложения --%>
				<c:forEach var="item" items="${message.attachList}">
					<c:url var="url" value="/user/file.do">
						<c:param name="id" value="${item.id}"/>
						<c:param name="title" value="${item.title}"/>
						<c:param name="secret" value="${item.secret}"/>
					</c:url>

					<div>
						 <input type="hidden" name="fileId" value="${item.id}"/>
						 <button class="btn-white btn-small mr1" type="button" onclick="if( confirm( 'Удалить вложение?' ) ){ $(this.parentNode).remove() }">X</button>
						 <a href="${url}">${item.title}</a>
					</div>
				</c:forEach>

				<%-- сюда генерируется список загруженных --%>
			</div>
			<button type="button" class="btn-white btn-small icon" onclick="$$.ajax.triggerUpload('${uploadFormId}');"><i class="ti-plus"></i></button>
		</div>
	</div>

	<div class="mt1 mb1">
		<ui:button type="ok" onclick="$$.ajax.post(this.form, {toPostNames: ['text']}).done(() => { $$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent()) })"/>
		<ui:button type="cancel" onclick="$('#${form.returnChildUiid}').empty();" styleClass="ml1"/>
	</div>
</html:form>

<%-- подготовка форм со специальными редакторами --%>
<c:forEach var="messageType" items="${config.typeMap.values()}">
	<c:if test="${messageType.specialEditor}">
		<html:form action="/user/message" styleId="${editorUiid}-${messageType.getClass().getName()}" styleClass="editorStopReload" style="display: none;">
			<input type="hidden" name="action" value="messageUpdate"/>
			<html:hidden property="processId"/>
			<html:hidden property="id"/>

			<c:set var="endpoint" value="user.process.message.editor.jsp"/>
			<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
		</html:form>
	</c:if>
</c:forEach>

<script>
	$(function()
	{
		${typeChangedScript}
	})
</script>
