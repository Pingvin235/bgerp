<%@page import="java.util.regex.Matcher"%>
<%@page import="java.net.MalformedURLException"%>
<%@page import="java.net.URL"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="ru.bgcrm.dao.message.MessageTypeEmail"%>
<%@page import="javax.mail.internet.InternetAddress"%>
<%@page import="ru.bgcrm.dao.message.MessageType"%>
<%@page import="ru.bgcrm.util.Utils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="ru.bgcrm.model.message.Message"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="formUiid" value="${u:uiid()}"/>

<c:set var="editorContainerUiid" value="${u:uiid()}" scope="request"/>

<c:set var="tagConfig" value="${u:getConfig(ctxSetup, 'ru.bgcrm.model.config.TagConfig')}"/>

<html:form action="/user/message" styleId="${formUiid}">
	<html:hidden property="action"/>
	<html:hidden property="processId"/>
	<html:hidden property="linkProcess"/>

	<c:url var="url" value="message.do">
		<c:param name="forward" value="processMessageEdit"/>
		<c:param name="areaId" value="process-message-add"/>
		<c:param name="processId" value="${form.param.processId}"/>
		<c:param name="returnChildUiid" value="${editorContainerUiid}"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	<button class="btn-green" type="button" onclick="$$.ajax.load('${url}', $('#${editorContainerUiid}'))">+</button>

	<html:hidden property="attach"/>
	<c:set var="sendCommand">$$.ajax.load($('#${formUiid}')[0], $('#${formUiid}').parent())</c:set>

	<c:set var="valuesHtml">
		<li value="0">${l.l('Все')}</li>
		<c:if test="${form.response.data.tagIds.contains(u:int(-1))}">
			<li value="-1">${l.l('Вложение')}</li>
		</c:if>
		<c:forEach var="item" items="${tagConfig.tagList}">
			<c:if test="${form.response.data.tagIds.contains(item.id)}">
				<li value="${item.id}">
					<span style="display: inline-block;
								 background-color: ${item.color}; width: 1em; height: 1em;">&nbsp;</span>
								 ${item.title}
				</li>
			</c:if>
		</c:forEach>
	</c:set>

	<ui:combo-single
		hiddenName="tagId" widthTextValue="100" styleClass="ml1" value="${form.param.tagId}"
		onSelect="${sendCommand}"
		valuesHtml="${valuesHtml}"/>

	<div style="display: inline-block; float: right;">
		<%-- без этого контейнера в хроме появляется лишний перенос --%>
		<div style="display: inline-block;">
			<c:set var="nextCommand" value="; ${sendCommand}"/>
			<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
		</div>
	</div>
</html:form>

<c:set var="config" value="${u:getConfig(ctxSetup, 'ru.bgcrm.dao.message.config.MessageTypeConfig')}"/>

<div id="${editorContainerUiid}"></div>

<% pageContext.setAttribute( "rChar", "\r" );
pageContext.setAttribute( "newLineChar", "\n" );
pageContext.setAttribute( "singleQuot", "'" );
%>

<c:set var="messagesUiid" value="${u:uiid()}"/>

<div class="mt1" id='${messagesUiid}'>
	<c:forEach var="message" items="${form.response.data.list}" varStatus="status">
		<c:set var="messageTextUiid" value="${u:uiid()}"/>

		<c:set var="style" value=""/>
		<c:if test="${form.param.markMessageId eq message.id }">
			<c:set var="style" value="border: 2px solid;"/>
		</c:if>

		<table class="hdata" style="width: 100%;">
			<c:set var="message" value="${message}" scope="request"/>
			<c:set var="messageType" value="${config.typeMap[message.typeId]}" scope="request"/>

			<c:set var="typeEmail" value="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeEmail'}"/>
			<c:set var="typeNote" value="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeNote'}"/>
			<c:set var="typeCall" value="${messageType.getClass().getName() eq 'ru.bgcrm.dao.message.MessageTypeCall'}"/>

			<c:set var="color" value="${messageType.getProcessMessageHeaderColor(message)}"/>

			<tr style="background-color: ${color}; ${style}">
				<td align="left" width="100%" style="text-align: left; vertical-align: middle;" class="in-table-cell">
					<div class="pr05">
						<%@ include file="message_direction.jsp"%>
						<%-- теги --%>
						<c:set var="messageTagIds" value="${form.response.data.messageTagMap[message.id]}"/>
						<c:forEach var="tagId" items="${messageTagIds}">
							<c:set var="tag" value="${tagConfig.tagMap[tagId]}"/>
							<span style="display: inline-block;
								background-color: ${tag.color}; width: 1em; height: 1em;"
								title="${tag.title}" class="mr05">&nbsp;</span>
						</c:forEach>
					</div>
					<c:if test="${typeNote}">
						<div style="width: 100%;">
							<div>
								#${message.id}&nbsp;${messageType.title}: ${message.subject}
								<c:if test="${message.processId ne form.param.processId}">
									${l.l('из')}&#32;<ui:process-link id="${message.processId}"/>
								</c:if>
							 </div>
							 <div class="mt05">
							 	Создано: ${u:formatDate( message.fromTime, 'ymdhm' )}
											(<ui:user-link id="${message.userId}"/>)
							 </div>
						</div>
					</c:if>
					<c:if test="${typeEmail}">
						<div style="width: 100%;">
							 <div>
							 	#${message.id} EMail [${messageType.email}]: ${message.subject}
							 </div>
							 <div class="mt05">
								<c:choose>
									<c:when test="${message.direction eq 1}">
										Отправлено: ${u:formatDate( message.fromTime, 'ymdhm' )} (<a href="mailto:${fn:escapeXml( message.from )}">${fn:escapeXml( message.from )}</a>) => ${fn:escapeXml( message.to )}
										<nobr>
											Обработано: ${u:formatDate( message.toTime, 'ymdhm' )}
											(<ui:user-link id="${message.userId}"/>)
										</nobr>
									</c:when>
									<c:otherwise>
										Создано: ${u:formatDate( message.fromTime, 'ymdhm' )} (<ui:user-link id="${message.userId}"/>)
										<nobr>
											Отправлено: ${u:formatDate( message.toTime, 'ymdhm' )} (<a href="mailto:${fn:escapeXml( message.to )}">${fn:escapeXml( message.to )}</a>)
										</nobr>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</c:if>
					<c:if test="${typeCall}">
						<div style="width: 100%;">
							 <div>
							 	#${message.id} Звонок "${messageType.title}"
							 </div>
							 <div class="mt05">
								<c:choose>
									<c:when test="${message.direction eq 1}">
										Принят: ${u:formatDate( message.fromTime, 'ymdhm' )} (<a href="mailto:${fn:escapeXml( message.from )}">${fn:escapeXml( message.from )}</a>) => ${fn:escapeXml( message.to )}
										<nobr>
											Обработано: ${u:formatDate( message.toTime, 'ymdhm' )} (<ui:user-link id="${message.userId}"/>)
										</nobr>
									</c:when>
									<c:otherwise>
										Создано: ${u:formatDate( message.fromTime, 'ymdhm' )} (<ui:user-link id="${message.userId}"/>)
										<nobr>
											Отправлено: ${u:formatDate( message.toTime, 'ymdhm' )} (<a href="mailto:${fn:escapeXml( message.to )}">${fn:escapeXml( message.to )}</a>)
										</nobr>
									</c:otherwise>
								</c:choose>
							</div>
						</div>
					</c:if>

					<c:set var="endpoint" value="user.process.message.header.jsp"/>
					<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
				</td>
				<td width="30px" style="white-space: nowrap;">
					<c:set var="actionButtonUiid" value="${u:uiid()}"/>

					<c:set var="linkFormUiid" value="${u:uiid()}"/>
					<html:form action="/user/message" styleId="${linkFormUiid}" style="white-space: nowrap; display: none;" styleClass="mr1" onsubmit="return false;">
						<input type="hidden" name="action" value="messageUpdateProcess"/>
						<input type="hidden" name="id" value="${message.id}"/>

						<c:set var="command">if( confirm( 'Привязать сообщение к указанному процессу?' ) && sendAJAXCommand( formUrl( this.form ) ) ){ openUrlToParent( '${form.requestUrl}', $('#${messagesUiid}') ) }</c:set>

						<input type="text" size="5" name="processId" class="ml1" onkeypress="if( enterPressed( event ) ){ ${command} }"/>
						<button type="button" class="btn-grey ml1" title="Привязать к другому процессу" onclick="${command}">OK</button>
						<button type="button" class="btn-grey ml1" onclick="$(this.form).hide();">Отмена</button>
					</html:form>

					<c:set var="tagFormUiid" value="${u:uiid()}"/>
					<html:form action="/user/message" styleId="${tagFormUiid}" style="white-space: nowrap; display: none;" styleClass="mr1" onsubmit="return false;">
						<input type="hidden" name="action" value="messageUpdateTags"/>
						<input type="hidden" name="id" value="${message.id}"/>

						<ui:select-mult list="${tagConfig.tagList}" values="${messageTagIds}" hiddenName="tagId"/>

						<br/>
						<c:set var="command">if (sendAJAXCommand(formUrl(this.form))) {openUrlToParent('${form.requestUrl}', $('#${messagesUiid}'))}</c:set>

						<button type="button" class="btn-grey ml1" title="Обновить теги" onclick="${command}">OK</button>
						<button type="button" class="btn-grey ml1" onclick="$(this.form).hide();$('#${actionButtonUiid}').show();">Отмена</button>
					</html:form>

					<c:set var="menuUiid" value="${u:uiid()}"/>
					<div style="height: 0px; max-height: 0px; width: 0px; max-width: 0px; display: inline-block;">
						<ul id="${menuUiid}" style="display: none;" class="menu">
							<c:set var="command">
								$('#${messageTextUiid} #msgBox').css( 'width', $('#${messageTextUiid} #msgBox').width() + 'px' );
								$('#${messageTextUiid} #msgBox').toggleClass( 'nowrap' );
								return false;
							</c:set>

							<li><a href="#UNDEF" onclick="${command}">Вкл./выкл. разрывы строк</a></li>
							<li><a href="#UNDEF" onclick="$('#${tagFormUiid}').css('display', 'inline-block'); $('#${actionButtonUiid}').hide(); return false;">Теги</a></li>
							
							<li>
								<a href="#">${l.l('Изменить процесс на')}</a>
								<ul>
									<li><a href="#UNDEF" onclick="$('#${linkFormUiid}').css('display', 'inline-block'); return false;">Другой существующий</a></li>

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
												if (confirm('${l.l('Изменить процесс сообщения на %s копию текущего процесса?', linkTypeTitle)}')) {
													<c:url var="url" value="message.do">
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
											<li><a href="#UNDEF" onclick="${command}">${l.l('%s копию текущего', linkTypeTitle)}</a></li>
										</c:forTokens>
									</c:if>
								</ul>
							</li>	

							<c:if test="${message.direction eq 1 and messageType.answerSupport}">
								<c:set var="subject" value="${message.subject}"/>
								<c:if test="${not fn:startsWith( subject, 'Re:' ) }">
									<c:set var="subject" value="Re: ${subject}"/>
								</c:if>

								<c:set var="answerText" value=">${message.text}"/>
								<c:set var="answerText" value="${fn:replace( answerText, rChar, '' )}"/>
								<c:set var="answerText" value="${fn:replace( answerText, newLineChar, newLineChar.concat( '>' ) )}"/>

								<%
									Message message = (Message)request.getAttribute( "message" );
									if( request.getAttribute( "messageType" ) instanceof MessageTypeEmail )
									{
										MessageTypeEmail type = (MessageTypeEmail)request.getAttribute( "messageType" );

										try
										{
											String answerTo = MessageTypeEmail.serializeAddresses( MessageTypeEmail.parseAddresses( message.getTo(), message.getFrom(), type.getEmail() ) );
											pageContext.setAttribute( "answerTo", answerTo );
										}
										catch( Exception e )
										{}
									}
								%>

								<c:url var="answerUrl" value="message.do">
									<c:param name="forward" value="processMessageEdit"/>
									<c:param name="processId" value="${message.processId}"/>
									<c:param name="messageType" value="${message.typeId}"/>
									<c:param name="subject" value="${fn:escapeXml( subject )}"/>
									<c:param name="to" value="${answerTo}"/>
									<c:param name="text" value="${fn:escapeXml( answerText )}"/>
									<c:param name="returnChildUiid" value="${editorContainerUiid}"/>
									<c:param name="returnUrl" value="${form.requestUrl}"/>
								</c:url>

								<li><a href="#UNDEF" onclick="
									$$.ajax.load('${answerUrl}', $('#${editorContainerUiid}'), {toPostNames: ['text']})
									.done(function () {$(window).scrollTop(150)});
									return false;">Ответить</a></li>
							</c:if>

							<c:if test="${messageType.isEditable(message)}">
								<c:url var="editUrl" value="message.do">
									<c:param name="forward" value="processMessageEdit"/>
									<c:param name="id" value="${message.id}"/>
									<c:param name="processId" value="${message.processId}"/>
									<c:param name="returnChildUiid" value="${editorContainerUiid}"/>
									<c:param name="returnUrl" value="${form.requestUrl}"/>
								</c:url>

								<li><a href="#UNDEF" onclick="if (bgcrm.lock.add('${message.lockEdit}')) {
									  $$.ajax.load('${editUrl}', $('#${editorContainerUiid}')).done(function () {$(window).scrollTop(150)});
									};
									return false;">Редактировать</a></li>
							</c:if>

							<c:if test="${messageType.isRemovable(message)}">
								<c:url var="deleteUrl" value="message.do">
									<c:param name="action" value="messageDelete"/>
									<c:param name="typeId-systemId" value="${message.typeId}-${message.id}"/>
								</c:url>

								<li><a href="#UNDEF" onclick="if( confirm('Удалить сообщение?') && sendAJAXCommand('${deleteUrl}') ){ openUrlToParent( '${form.requestUrl}', $('#${editorContainerUiid}') ) };  return false;">Удалить</a></li>
							</c:if>
						 </ul>
					 </div>

					<c:set var="showMenuCode">
						const $menu = $('#${menuUiid}')
						
						$menu.menu().show().position({
							my: 'right top',
							at: 'right bottom',
							of: this
						});

						$(document).one('click', function () {
							$menu.hide();
						});

						event.stopPropagation();
					</c:set>

					<button id="${actionButtonUiid}" class="btn-white" onclick="${showMenuCode}" title="Действие">М</button>
				</td>
			</tr>
		</table>

		<%-- разделено на отдельные таблицы, т.к. в случае общей таблицы Chrome начинает увеличивать размер правой верхней ячейки с кнопками --%>
		<table id="${messageTextUiid}" class="hdata" style="width: 100%; table-layout:fixed;">
			<tr>
				<td id="msgBox" style="border-top: none; display: block; overflow-x: auto; padding-bottom: 0.5em; word-wrap: break-word;">
					<c:set var="text" value="${message.text}"/>
					<%@ include file="email_text_prepare.jsp"%>

					<c:set var="text" value="${u:htmlEncode(text)}"/>

					<%
						// ссылки на открытие процессов
						String tx = (String)pageContext.getAttribute( "text" );
						tx = tx.replaceAll( "#(\\d+)", "<a onclick='openProcess($1); return false;' href='#UNDEF'>$0</a>" );

						// выделение URLов ссылками
						// взято отсюда: http://blog.codinghorror.com/the-problem-with-urls/
						// там ещё есть обработка скобок
						Pattern pattern = Pattern.compile("\\(?\\bhttps?://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
						Matcher m = null;
						int pos = 0;

						while ((m = pattern.matcher(tx)).find(pos)) {
							String url = m.group();
							String link = "<a target=\"_blank\" href=\"" + url + "\">" + url + "</a>";
							tx = tx.substring(0, m.start()) +
								link +
								tx.substring(m.end());
							pos = m.start() + link.length();
						}

						pageContext.setAttribute("text", tx);
					%>

					${text}

					<%-- TODO: может, вернуть опционально впоследствии
					<c:choose>
						<c:when test="${status.first}">${u:htmlEncode(text)}</c:when>
						<c:otherwise>
							<c:set var="text" value="${u:htmlEncode(text)}"/>
							<c:set var="maxLength" value="500"/>
							<%@ include file="/WEB-INF/jspf/short_text.jsp"%>
						</c:otherwise>
					</c:choose>
					--%>
				</td>
			</tr>

			<c:if test="${not empty message.attachList}">
				<tr>
					<td style="border-top: none; display: block;">
						Вложения:
						<c:if test="${typeEmail or typeNote}">
							<c:forEach var="item" items="${message.attachList}" varStatus="status">
								<c:url var="url" value="../user/file.do">
									<c:param name="id" value="${item.id}"/>
									<c:param name="title" value="${item.title}"/>
									<c:param name="secret" value="${item.secret}"/>
								</c:url>
								<a href="${url}" class="preview">${item.title}</a><c:if test="${not status.last}">, </c:if>
							</c:forEach>
						</c:if>

						<c:set var="endpoint" value="user.process.message.attaches.jsp"/>
						<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
					</td>
				</tr>
			</c:if>
		</table>
	</c:forEach>
</div>

<%-- параметр nextCommand здесь используется из предыдущего промотчика, перед списком сообщений --%>
<c:set var="pageFormSelector" value="#${formUiid}"/>
 <%@ include file="/WEB-INF/jspf/page_control.jsp"%>

<script>
	$(function () {
		$('#${messagesUiid} a.preview').preview();
	})
</script>