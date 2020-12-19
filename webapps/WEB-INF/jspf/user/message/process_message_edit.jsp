<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uploadFormId" value="${u:uiid()}"/>
<c:set var="uploadListId" value="${u:uiid()}"/>

<c:set var="message" scope="request" value="${form.response.data.message}"/>
<c:set var="config" value="${u:getConfig( ctxSetup, 'ru.bgcrm.dao.message.config.MessageTypeConfig' ) }"/>

<form id="${uploadFormId}" action="../user/file.do" method="POST" enctype="multipart/form-data" name="form" style="position: absolute; top: -100px;">
	<input type="hidden" name="action" value="temporaryUpload"/>
	<input type="hidden" name="responseType" value="json"/>
	<input type="file" name="file" onchange="$(this.form).submit();"/>
</form>


<script>
	$(function()
	{
		$('#${uploadFormId}').iframePostForm
		({
			json : true,
			post : function()
			{
				if( $('#${uploadFormId} input[type=file]').val().length == 0 )
				{
					alert( "Не выбран файл!" );
					return false;
				}
			},
			complete : function( response )
			{
				var fileId = response.data.file.id;
				var fileTitle = response.data.file.title;

				var deleteCode = "if( sendAJAXCommand( '../user/file.do?action=temporaryDelete&id=" + fileId + "') ){ $(this.parentNode).remove() }";

				$('#${uploadListId}').append(
					"<div>" +
						 "<input type=\"hidden\" name=\"tmpFileId\" value=\""+ fileId + "\"/>" +
						 "<button class=\"btn-white btn-small mr1\" type=\"button\" value=\"X\" onclick=\"" + deleteCode + "\">X</button>" + fileTitle +
					 "</div>"
				);
			}
		});
	});
</script>

<c:set var="editorUiid" value="${u:uiid()}"/>
<c:set var="typeComboUiid" value="${u:uiid()}"/>

<c:set var="typeChangedScript">
	var typeId = $('#${typeComboUiid}').find('input[name=typeId]').val();
	var $selectedTypeLi = $('#${typeComboUiid} ul.drop li[value=' + typeId + ']');

	var editor = $selectedTypeLi.attr('editor');
	var $activeEditor = $('#${editorUiid}');
	var $editorParent = $activeEditor.parent();

	if (editor) {
		$activeEditor = $('form[id=\'${editorUiid}-' + editor + '\']');
	} else {
		$('#${editorUiid} div#subject').toggle( $selectedTypeLi.attr( 'subject' ) == 'true' );
		$('#${editorUiid} div#address').toggle( $selectedTypeLi.attr( 'address' ) == 'true' );
		$('#${editorUiid} div#attach').toggle( $selectedTypeLi.attr( 'attach' ) == 'true' );
	}

	$editorParent.find('>form').hide();
	$activeEditor.show();

	$('#${typeComboUiid}').detach().appendTo($activeEditor.find('#typeSelectContainer'));
</c:set>

<html:form action="/user/message" styleId="${editorUiid}" styleClass="editorStopReload">
	<input type="hidden" name="action" value="messageUpdate"/>
	<html:hidden property="processId"/>
	<html:hidden property="id"/>

	<c:if test="${not empty message}">
		<input type="hidden" id="lock-${message.lockEdit}" name="lockFree"/>
	</c:if>

	<div style="display: table; width: 100%;">
		<div class="in-table-cell">
			<div style="vertical-align: top; width: 30px;" id="typeSelectContainer">
				<h2>Тип</h2>

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
				<h2>Получатель</h2>
				<input type="text" name="to" style="width: 100%;" placeholder="Для EMail: addr1@domain.com, addr2@domain.com; CC: copy1@domain.com, copy2.domain.com" value="${message.to}"/>
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
			<h2>Тема</h2>
			<input type="text" name="subject" style="width: 100%;" value="${message.subject}"/>
		</div>
		<div>
			<h2>Сообщение</h2>
			<textarea rows="20" style="width: 100%; resize: vertical;" name="text" class="tabsupport">${message.text}</textarea>
			<span class="hint">Вы можете использовать #код для ссылок на другие процессы, подобные записи будут автоматически преобразованы в ссылки открытия карточек.</span>
		</div>
		<div id="attach">
			<h2>Вложения</h2>
			<div id="${uploadListId}" class="in-mb05-all">
				<%-- уже загруженные вложения --%>
				<c:forEach var="item" items="${message.attachList}">
					<c:url var="url" value="../user/file.do">
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
			<button type="button" class="btn-white btn-small" onclick="$('#${uploadFormId}').find('input[name=file]').click();">+</button>
		</div>
	</div>

	<div class="mt1 mb1">
		<button class="btn-grey" type="button" onclick="if( sendAJAXCommand( formUrl( this.form ), ['text'] ) ){ openUrlToParent( '${form.returnUrl}', $('#${form.returnChildUiid}') ) }">ОК</button>
		<button class="btn-grey ml1" type="button" onclick="$('#${form.returnChildUiid}').empty();">Отмена</button>
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