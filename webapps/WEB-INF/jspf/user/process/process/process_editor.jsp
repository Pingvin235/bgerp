<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- режим 'card' - отдельная карточка, 'linked' - просмотр привязанной --%>
<c:set var="mode" value="${form.param['mode']}"/>
<c:if test="${empty mode}">
	<c:set var="mode" value="card"/>
</c:if>

<c:if test="${mode ne 'card'}">
	<ui:button type="close" styleClass="mb1" onclick="$$.ajax.load('${form.returnUrl}', $('#${tableId}').parent())"/>
</c:if>

<%-- when not existing process is opened processType has not set --%>
<c:if test="${not empty processType}">
	<%-- tableId очень важный идентификатор - нужен для определения в DOM дереве расположения редактора данного процесса --%>
	<table id="${tableId}" style="width: 100%;" class="nopad">
		<tr>
			<%-- TODO: в будущем, можно и порядок табов задать тоже, ещё JEXL условие прикрутить --%>
			<c:set var="components" value="${u:toList( 'header,status,description,executors,links,params' )}"/>
			<c:if test="${not empty processType}">
				<c:set var="componentsConfig" value="${processType.properties.configMap.getConfig('ru.bgcrm.model.process.config.ProcessCardConfig')}"/>
			</c:if>
			<c:set var="item" value="${componentsConfig.getItem( mode )}"/>
			<c:if test="${not empty item}">
				<c:set var="components" value="${item.componentList}"/>
			</c:if>

			<c:choose>
				<c:when test="${mode eq 'card'}">
					<c:set var="leftStyle">${processType.properties.configMap['style.processCardLeftBlock']}</c:set>
					<c:if test="${empty leftStyle}"><c:set var="leftStyle">width: 50%;</c:set></c:if>

					<c:set var="rightStyle">${processType.properties.configMap['style.processCardRightBlock']}</c:set>
					<c:if test="${empty rightStyle}"><c:set var="rightStyle">width: 50%;</c:set></c:if>
				</c:when>
				<c:when test="${mode eq 'linked'}">
					<c:set var="leftStyle">width: 100%;</c:set>
					<c:set var="rightStyle">display: none;</c:set>
				</c:when>
			</c:choose>

			<td id="processLeftDiv" valign="top" style="${leftStyle}">
			<div class="wrap">
				<c:if test="${mode eq 'card' and not empty processType}">
					<u:newInstance var="ifaceStateDao" clazz="ru.bgcrm.dao.IfaceStateDAO">
						<u:param value="${ctxConSet.getSlaveConnection()}"/>
					</u:newInstance>
					<c:set var="ifaceStateMap" value="${ifaceStateDao.getIfaceStates('process', process.id)}"/>

					<script>
						$(function () {
							var $tabs = $("#${tableId} #processTabsDiv").tabs({refreshButton: true});

							<%-- зависимые процессы --%>

							<%-- 2 - отображение в теле процесса --%>
							<c:if test="${processType.properties.configMap.getSok('', false, 'show.tab.links', 'processShowLinks') eq '1'}">
								<%-- TODO: ifaceState 'links' --%>
								<c:url var="url" value="/user/link.do">
									<c:param name="action" value="linkList"/>
									<c:param name="id" value="${process.id}"/>
									<c:param name="objectType" value="process"/>
									<c:param name="processTypeId" value="${process.typeId}"/>
								</c:url>

								$tabs.tabs( "add", "${url}", "${l.l('Привязки')}" );
							</c:if>

							<c:if test="${processType.properties.configMap.getSok('1', false, 'show.tab.messages', 'processShowMessages') eq '1'}">
								<c:set var="ifaceState" value="${ifaceStateMap['messages']}"/>

								<c:url var="url" value="/user/message.do">
									<c:param name="action" value="processMessageList"/>
									<c:param name="processId" value="${process.id}"/>
									<c:param name="ifaceState" value="${ifaceState.state}"/>
									<c:param name="linkProcess" value="${processType.properties.configMap['show.messages.link.process']}"/>
								</c:url>

								$tabs.tabs( "add", "${url}", "${l.l('Сообщения')}${ifaceState.getFormattedState()}", " id='process-messages'" );
							</c:if>

							<c:if test="${processType.properties.configMap.getSok('1', false, 'show.tab.links.process', 'processShowProcessLinks') eq '1'}">
								<c:set var="ifaceId" value="link_process"/>
								<c:set var="ifaceState" value="${ifaceStateMap[ifaceId]}"/>

								<c:url var="url" value="/user/process/link.do">
									<c:param name="action" value="linkProcessList"/>
									<c:param name="id" value="${process.id}"/>
									<c:param name="linkedReferenceName" value="linkedProcessList"/>
									<c:param name="linkReferenceName" value="linkProcessList"/>
									<c:param name="ifaceId" value="${ifaceId}"/>
									<c:param name="ifaceState" value="${ifaceState.state}"/>
								</c:url>

								$tabs.tabs( "add", "${url}", "${l.l('Связанные процессы')}${ifaceState.getFormattedState()}" );
							</c:if>

							<%-- TODO: Extract to plugin Callboard --%>
							<c:set var="timeSetConfig" value="${processType.properties.configMap.getConfig('ru.bgcrm.model.work.config.ProcessTimeSetConfig')}"/>
							<c:if test="${not empty timeSetConfig.callboard}">
								<c:url var="url" value="work.do">
									<c:param name="action" value="processTime"/>
									<c:param name="processId" value="${process.id}"/>
								</c:url>

								$tabs.tabs( "add", "${url}", "Уст. времени" );
							</c:if>

							<c:set var="endpoint" value="user.process.tabs.jsp"/>
							<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>

							var $leftTd = $('#${tableId} > tbody > tr > td#processLeftDiv');
							var $leftDivWrap = $leftTd.find('> .wrap');

							// ниже логика сокрытия левого блока карточки процесса когда он перестаёт быть видимым,
							// с растяжением на весь экран правого блока, где может быть список сообщений

							// сохранённые параметры левого блока (устанавливаются при скрытии)
							var state = null;

							$(window).scroll(function() {
								const hide = function () {
									state = {
										height: $leftDivWrap.height(),
										minWidth: $leftTd.css("min-width")
									};
									$leftDivWrap.css("height", state.height).find(">div").hide();
									$leftTd.css("min-width", "5px");
								}

								const show = function () {
									$leftDivWrap.css("height", "").find(">div").show();
									$leftTd.css("min-width", state.minWidth);
									state = null;
								}

								if (state) {
									if (bgcrm.isElementInView($leftDivWrap, 0))
										show();
								} else if (!bgcrm.isElementInView($leftDivWrap, 100))
									hide();
							});
						})
					</script>
				</c:if>

				<c:forEach var="c" items="${components}">
					<c:choose>
						<c:when test="${c eq 'header'}">
							<%@ include file="/WEB-INF/jspf/user/process/process/process_header.jsp"%>
						</c:when>
						<c:when test="${c eq 'status' and not empty processType}">
							<%@ include file="/WEB-INF/jspf/user/process/process/process_status.jsp"%>
						</c:when>
						<c:when test="${c eq 'description'}">
							<%@ include file="/WEB-INF/jspf/user/process/process/process_description.jsp"%>
						</c:when>
						<c:when test="${c eq 'executors'}">
							<c:if test="${processType.properties.configMap['hideExecutors'] ne 1}">
								<%@ include file="/WEB-INF/jspf/user/process/process/process_executors.jsp"%>
							</c:if>
						</c:when>
						<c:when test="${c eq 'links'}">
							<%-- исключить отображение привязок в случае, если они отображаются справа --%>
							<c:if test="${processType.properties.configMap.getSok('', false, 'show.tab.links', 'processShowLinks') ne '1'}">
								<div>
									<c:url var="url" value="/user/link.do">
										<c:param name="action" value="linkList"/>
										<c:param name="id" value="${process.id}"/>
										<c:param name="objectType" value="process"/>
										<c:param name="header" value="${l.l('Привязки')}"/>
										<c:param name="processTypeId" value="${process.typeId}"/>
									</c:url>

									<c:remove var="form"/>
									<c:import url="${url}"/>
								</div>
							</c:if>
						</c:when>
						<c:when test="${c eq 'params' and not empty processType}">
							<%@ include file="/WEB-INF/jspf/user/process/process/process_parameters.jsp"%>
						</c:when>
						<c:when test="${fn:startsWith( c, 'jsp:')}">
							<c:set var="jsp" value="${fn:substringAfter( c, 'jsp:')}"/>
							<jsp:include page="${jsp}"/>
						</c:when>
					</c:choose>
				</c:forEach>
			</div>
			</td>
			<td style="${rightStyle}" valign="top" class="pl1">
				<div id="processTabsDiv">
					<ul></ul>
				</div>
			</td>
		</tr>
	</table>
</c:if>

<%@ include file="process_title.jsp"%>
