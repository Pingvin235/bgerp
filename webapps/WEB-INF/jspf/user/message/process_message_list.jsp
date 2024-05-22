<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="formUiid" value="${u:uiid()}"/>
<c:set var="editorContainerUiid" value="${u:uiid()}" scope="request"/>

<c:set var="tagConfig" value="${ctxSetup.getConfig('ru.bgcrm.model.message.TagConfig')}"/>
<c:set var="TAG_ATTACH_ID"><%=ru.bgcrm.model.message.TagConfig.Tag.TAG_ATTACH_ID%></c:set>
<c:set var="TAG_PIN_ID"><%=ru.bgcrm.model.message.TagConfig.Tag.TAG_PIN_ID%></c:set>

<c:set var="ACTION_MODIFY_NOT_OWNED"><%=ru.bgcrm.struts.action.MessageAction.ACTION_MODIFY_NOT_OWNED%></c:set>

<c:set var="allowedTags" value="${ctxUser.checkPerm('ru.bgcrm.struts.action.MessageAction:messageUpdateTags')}"/>

<ui:when type="user">
	<html:form action="${form.httpRequestURI}" styleId="${formUiid}">
		<html:hidden property="action"/>
		<html:hidden property="processId"/>
		<html:hidden property="linkProcess"/>

		<c:url var="url" value="${form.httpRequestURI}">
			<c:param name="action" value="processMessageEdit"/>
			<c:param name="areaId" value="process-message-add"/>
			<c:param name="processId" value="${form.param.processId}"/>
			<c:param name="returnChildUiid" value="${editorContainerUiid}"/>
			<c:param name="returnUrl" value="${form.requestUrl}"/>
		</c:url>
		<ui:button type="add" onclick="$$.ajax.load('${url}', $('#${editorContainerUiid}'))"/>

		<html:hidden property="attach"/>
		<c:set var="sendCommand">$$.ajax.load($('#${formUiid}')[0], $('#${formUiid}').parent())</c:set>

		<c:set var="valuesHtml">
			<li value="0">${l.l('Все')}</li>
			<c:if test="${frd.tagIds.contains(u:int(TAG_ATTACH_ID))}">
				<li value="${TAG_ATTACH_ID}">${l.l('Вложение')}</li>
			</c:if>
			<c:forEach var="item" items="${tagConfig.tagList}">
				<c:if test="${frd.tagIds.contains(item.id)}">
					<li value="${item.id}">
						<span class="process-message-tag" style="background-color: ${item.color};">&nbsp;</span>
						${item.title}
					</li>
				</c:if>
			</c:forEach>
		</c:set>

		<ui:combo-single
			hiddenName="tagId" widthTextValue="10em" styleClass="ml1" value="${form.param.tagId}"
			onSelect="${sendCommand}"
			valuesHtml="${valuesHtml}"/>

		<div style="float: right;">
			<%-- без этого контейнера в хроме появляется лишний перенос --%>
			<div style="display: inline-block;">
				<ui:page-control nextCommand="; ${sendCommand}" />
			</div>
		</div>
	</html:form>

	<div id="${editorContainerUiid}"></div>
</ui:when>

<c:set var="config" value="${ctxSetup.getConfig('ru.bgcrm.model.message.config.MessageTypeConfig')}"/>

<c:set var="messagesUiid" value="${u:uiid()}"/>
<div class="mt1" id='${messagesUiid}'>
	<c:forEach var="message" items="${frd.list}" varStatus="status">
		<c:set var="messageTextUiid" value="${u:uiid()}"/>

		<c:set var="style" value=""/>
		<c:if test="${form.param.markMessageId eq message.id }">
			<c:set var="style" value="border: 2px solid;"/>
		</c:if>

		<table class="hdata">
			<c:set var="message" value="${message}" scope="request"/>
			<c:set var="messageType" value="${config.typeMap[message.typeId]}" scope="request"/>

			<c:set var="className" value="${messageType.getClass().getSimpleName()}"/>
			<c:set var="typeNote" value="${className eq 'MessageTypeNote'}"/>
			<c:set var="typeCall" value="${className eq 'MessageTypeCall'}"/>
			<c:set var="typeUnknown" value="${className eq 'MessageTypeConfig$MessageTypeUnknown'}"/>

			<c:set var="color" value="${messageType.getProcessMessageHeaderColor(message)}"/>
			<c:set var="unreadBold" value="${message.unread ? 'font-weight: bold; ' : ''}"/>

			<tr style="background-color: ${color}; ${style}">
				<td style="vertical-align: middle; ${unreadBold}" class="in-table-cell w100p">
					<%-- tags column --%>
					<c:set var="messageTagIds" value="${frd.messageTagMap[message.id]}"/>
					<c:set var="pinned" value="${messageTagIds.contains(u:int(TAG_PIN_ID))}"/>
					<c:set var="tags">
						<c:if test="${pinned}">
							<c:set var="spanAttrs">
								<c:if test="${allowedTags}">
									<c:url var="url" value="${form.httpRequestURI}">
										<c:param name="action" value="messageToggleTags"/>
										<c:param name="id" value="${message.id}"/>
										<c:param name="tagId" value="${TAG_PIN_ID}"/>
									</c:url>
									style="cursor: pointer;"
									title="${l.l('Unpin')}"
									onclick="$$.ajax.post('${url}').done(() => $$.ajax.load('${form.requestUrl}', document.getElementById('${formUiid}').parentElement))"; return false;
								</c:if>
							</c:set>
							<span ${spanAttrs} class="process-message-tag ti-pin-alt"></span>
						</c:if>
						<c:forEach var="tagId" items="${messageTagIds}">
							<c:set var="tag" value="${tagConfig.tagMap[tagId]}"/>
							<c:if test="${not empty tag}">
								<span class="process-message-tag" style="background-color: ${tag.color};" title="${tag.title}"></span>
							</c:if>
						</c:forEach>
					</c:set>
					<c:if test="${not empty tags}">
						<div class="pr05">
							${tags}
						</div>
					</c:if>

					<c:set var="headerJsp" value="${messageType.headerJsp}"/>

					<c:choose>
						<c:when test="${not empty headerJsp}">
							<plugin:include endpoint="${headerJsp}"/>
						</c:when>
						<c:when test="${typeNote}">
							<div style="width: 100%;">
								<div>
									#${message.id}&nbsp;${messageType.title} | ${message.subject}
									<c:if test="${message.processId ne form.param.processId}">
										&#32;${l.l('из')}&#32;<ui:process-link id="${message.processId}"/>
									</c:if>
								</div>
								<div class="mt05">
									${l.l('Создано')}: ${tu.format(message.fromTime, 'ymdhm')} (<ui:user-link id="${message.userId}"/>)
								</div>
							</div>
						</c:when>
						<c:when test="${typeCall}">
							<div style="width: 100%;">
								<div>
									#${message.id}&nbsp;${l.l('Звонок')} "${messageType.title}"
								</div>
								<div class="mt05">
									<c:choose>
										<c:when test="${message.direction eq 1}">
											${l.l('Принят')}: ${tu.format(message.fromTime, 'ymdhm')} (<a href="mailto:${u.escapeXml(message.from)}">${u.escapeXml(message.from)}</a>) => ${u.escapeXml(message.to)}
											<nobr>
												${l.l('Обработано')}: ${tu.format(message.toTime, 'ymdhm')} (<ui:user-link id="${message.userId}"/>)
											</nobr>
										</c:when>
										<c:otherwise>
											${l.l('Создано')}: ${tu.format(message.fromTime, 'ymdhm')} (<ui:user-link id="${message.userId}"/>)
											<nobr>
												${l.l('Отправлено')}: ${tu.format(message.toTime, 'ymdhm')} (<a href="mailto:${u.escapeXml(message.to)}">${u.escapeXml(message.to)}</a>)
											</nobr>
										</c:otherwise>
									</c:choose>
								</div>
							</div>
						</c:when>
						<c:otherwise>
							<div>
								#${message.id}&nbsp;${l.l('Несуществующий тип')} ID: ${messageType.id}
							</div>
						</c:otherwise>
					</c:choose>
				</td>
				<td width="30px" style="white-space: nowrap;">
					<c:set var="actionButtonUiid" value="${u:uiid()}"/>
					<%-- previous td with message header --%>
					<c:set var="actionButtonHideArea">$('#${actionButtonUiid}').closest('td').prev()</c:set>
					<c:set var="actionButtonStartEdit">.show(); $('#${actionButtonUiid}').parent().find('>button').hide(); ${actionButtonHideArea}.hide(); return false;</c:set>
					<c:set var="actionButtonCancelEdit">$(this.form).hide(); $('#${actionButtonUiid}').parent().find('>button').show(); ${actionButtonHideArea}.show();</c:set>

					<c:set var="linkFormUiid" value="${u:uiid()}"/>
					<html:form action="${form.httpRequestURI}" styleId="${linkFormUiid}" style="display: none;" onsubmit="return false;">
						<input type="hidden" name="action" value="messageUpdateProcess"/>
						<input type="hidden" name="id" value="${message.id}"/>

						<c:set var="command">
							if (confirm('${l.l('Link the message to the process?')}'))
								$$.ajax.post(this).done(() => {
									$$.ajax.load('${form.requestUrl}', $('#${messagesUiid}').parent());
								});
						</c:set>

						<c:set var="targetProcessUiid" value="${u:uiid()}"/>
						<ui:combo-single hiddenName="processId" id="${targetProcessUiid}" style="width: 100%;"/>

						<c:set var="actionButtonStartEditMerge">
							const processList = openedObjectList({'typesInclude' : ['process']});
							let html = '';
							$.each(processList, function() {
								html += '<li value=\'' + this.id + '\'>' + this.title + '</li>';
							});

							$('#${targetProcessUiid} ul.drop').html(html);
							$$.ui.comboSingleInit($('#${targetProcessUiid}'));
						</c:set>

						<%@ include file="process_message_list_editor_buttons.jsp"%>
					</html:form>

					<c:set var="tagFormUiid" value="${u:uiid()}"/>
					<html:form action="${form.httpRequestURI}" styleId="${tagFormUiid}" style="display: none;" onsubmit="return false;">
						<input type="hidden" name="action" value="messageUpdateTags"/>
						<input type="hidden" name="id" value="${message.id}"/>

						<ui:select-mult list="${tagConfig.tagList}" values="${messageTagIds}" hiddenName="tagId"/>

						<c:set var="command">$$.ajax.post(this).done(() => { $$.ajax.load('${form.requestUrl}', $('#${messagesUiid}').parent()) });</c:set>

						<%@ include file="process_message_list_editor_buttons.jsp"%>
					</html:form>

					<c:url var="readUrl" value="${form.httpRequestURI}">
						<c:param name="action" value="messageUpdateRead"/>
						<c:param name="id" value="${message.id}"/>
					</c:url>

					<c:set var="menuUiid" value="${u:uiid()}"/>
					<ui:popup-menu id="${menuUiid}" style="display: inline-block;">
						<li><a href="#" onclick="$$.message.lineBreak(this, '${messageTextUiid}'); return false;">
							<i class="ti-check" style="display: none;"></i>
							${l.l('Вкл./выкл. разрывы строк')}
						</a></li>
						<ui:when type="user">
							<c:if test="${allowedTags}">
								<c:if test="${not pinned}">
									<c:url var="url" value="${form.httpRequestURI}">
										<c:param name="action" value="messageToggleTags"/>
										<c:param name="id" value="${message.id}"/>
										<c:param name="tagId" value="${TAG_PIN_ID}"/>
										<c:param name="add" value="1"/>
									</c:url>
									<li><a href="#" onclick="
										$$.ajax.post('${url}').done(() => {
											$$.ajax.load('${form.requestUrl}', $('#${formUiid}').parent());
										});
										return false;
									"><i class="ti-pin-alt"></i>&nbsp;${l.l('Pin')}</a></li>
								</c:if>

								<li><a href="#" onclick="$('#${tagFormUiid}')${actionButtonStartEdit}">${l.l('Tags')}</a></li>
							</c:if>

							<c:if test="${messageType.readable and message.read and ctxUser.checkPerm('ru.bgcrm.struts.action.MessageAction:messageUpdateRead')}">
								<li><a href="#" onclick="
									$$.ajax.post('${readUrl}&value=0').done(() => {
										$$.ajax.load('${form.requestUrl}', $('#${editorContainerUiid}').parent());
									});
									">${l.l('Mark as unread')}</a></li>
							</c:if>
							<li>
								<a href="#">${l.l('Изменить процесс на')}</a>
								<ul>
									<li><a href="#" onclick="${actionButtonStartEditMerge} $('#${linkFormUiid}')${actionButtonStartEdit}">${l.l('Другой существующий')}</a></li>

									<c:if test="${messageType.processChangeSupport}">
										<c:forTokens items = " ,processDepend,processMade,processLink" delims = "," var = "linkType">
											<c:set var="linkTypeTitle">
												<c:choose>
													<c:when test="${linkType eq 'processDepend'}">${l.l('Зависящую')}</c:when>
													<c:when test="${linkType eq 'processMade'}">${l.l('Порождённую')}</c:when>
													<c:when test="${linkType eq 'processLink'}">${l.l('Ссылаемую')}</c:when>
													<c:otherwise>${l.l('Независимую')}</c:otherwise>
												</c:choose>
											</c:set>
											<c:set var="command">
												if (confirm('${l.l('Изменить процесс сообщения на {} копию текущего процесса?', linkTypeTitle)}')) {
													<c:url var="url" value="${form.httpRequestURI}">
														<c:param name="action" value="messageUpdateProcessToCopy"/>
														<c:param name="id" value="${message.id}"/>
														<c:param name="linkType" value="${linkType}"/>
													</c:url>
													$$.ajax.post('${url}').done((result) => {
														const processId = result.data.process.id;
														$$.process.open(processId);
														// $('div#process-' + processId + ' #processTabsDiv').tabs('showTab', 'process-messages');
													});
												}
												return false;
											</c:set>
											<li><a href="#" onclick="${command}">${l.l('{} копию текущего', linkTypeTitle)}</a></li>
										</c:forTokens>
									</c:if>
								</ul>
							</li>

							<c:remove var="answerCommand"/>
							<c:if test="${message.direction eq 1 and messageType.answerSupport}">
								<c:url var="answerUrl" value="${form.httpRequestURI}">
									<c:param name="action" value="processMessageEdit"/>
									<c:param name="returnChildUiid" value="${editorContainerUiid}"/>
									<c:param name="returnUrl" value="${form.requestUrl}"/>
									<c:param name="processId" value="${message.processId}"/>
									<c:param name="replyToId" value="${message.id}"/>
								</c:url>

								<c:set var="answerCommand" value="$$.ajax
										.load('${answerUrl}', $('#${editorContainerUiid}'))
										.done(function () {$(window).scrollTop(150)});"/>

								<li><a href="#" onclick="${answerCommand}return false;">${l.l('Ответить')}</a></li>
							</c:if>

							<c:if test="${form.user.id == message.userId or ctxUser.checkPerm(ACTION_MODIFY_NOT_OWNED)}">
								<c:if test="${messageType.isEditable(message) and ctxUser.checkPerm('ru.bgcrm.struts.action.MessageAction:processMessageEdit')}">
									<c:url var="editUrl" value="${form.httpRequestURI}">
										<c:param name="action" value="processMessageEdit"/>
										<c:param name="id" value="${message.id}"/>
										<c:param name="processId" value="${message.processId}"/>
										<c:param name="returnChildUiid" value="${editorContainerUiid}"/>
										<c:param name="returnUrl" value="${form.requestUrl}"/>
									</c:url>

									<li><a href="#" onclick="$$.lock.add('${message.lockEdit}').done(() => {
											$$.ajax.load('${editUrl}', $('#${editorContainerUiid}')).done(() => $(window).scrollTop(150));
										});
										return false;">${l.l('Редактировать')}</a></li>
								</c:if>

								<c:if test="${messageType.isRemovable(message) and ctxUser.checkPerm('ru.bgcrm.struts.action.MessageAction:messageDelete')}">
									<c:url var="deleteUrl" value="${form.httpRequestURI}">
										<c:param name="action" value="messageDelete"/>
										<c:param name="typeId-systemId" value="${message.typeId}-${message.id}"/>
									</c:url>

									<li><a href="#" onclick="
										if (confirm('${l.l('Удалить сообщение?')}'))
											$$.ajax.post('${deleteUrl}').done(() => {
												$$.ajax.load('${form.requestUrl}', $('#${formUiid}').parent());
											});
										return false;
										">${l.l('Удалить')}</a></li>
								</c:if>
							</c:if>

						</ui:when>
					</ui:popup-menu>

					<c:if test="${messageType.readable and message.unread and ctxUser.checkPerm('ru.bgcrm.struts.action.MessageAction:messageUpdateRead')}">
						<button class="btn-white icon mr05" title="${l.l('Mark as read')}" onclick="
							$$.ajax.post('${readUrl}&value=1').done(() => {
								$$.ajax.load('${form.requestUrl}', $('#${formUiid}').parent());
							});
						"><i class="ti-eye"></i></button>
					</c:if>

					<c:if test="${not empty answerCommand}">
						<ui:button type="reply" onclick="${answerCommand}" styleClass="mr05"/>
					</c:if>

					<ui:button id="${actionButtonUiid}" type="more" onclick="$$.ui.menuInit($('#${actionButtonUiid}'), $('#${menuUiid}'), 'right', true);"/>
				</td>
			</tr>
		</table>

		<%-- разделено на отдельные таблицы, т.к. в случае общей таблицы Chrome начинает увеличивать размер правой верхней ячейки с кнопками --%>
		<table id="${messageTextUiid}" class="hdata" style="width: 100%; table-layout:fixed;">
			<tr>
				<td id="msgBox" style="border-top: none; padding-bottom: 0.5em;">
					<pre class="pre-wrap" style="overflow-x: auto;"><ui:text-prepare text="${message.text}"/></pre>
				</td>
			</tr>

			<c:if test="${not empty message.attachList}">
				<tr>
					<td style="border-top: none; display: block;">
						${l.l('Вложения')}:
						<c:if test="${typeNote}">
							<%@ include file="process_message_attachments.jsp"%>
						</c:if>
						<plugin:include endpoint="user.process.message.attachments.jsp"/>
					</td>
				</tr>
			</c:if>
		</table>
	</c:forEach>
</div>

<ui:when type="user">
	<ui:page-control pageFormId="${formUiid}" nextCommand="; ${sendCommand}" />

	<script>
		$(function () {
			$('#${messagesUiid} a.preview').preview();
		})
	</script>
</ui:when>
