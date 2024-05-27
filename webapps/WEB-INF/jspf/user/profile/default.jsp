<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="requestUserId" value="${form.param.userId}" />
<c:set var="uiid" value="${u:uiid()}"/>

<c:url var="createUrl" value="news.do">
	<c:param name="method" value="newsEdit"></c:param>
	<c:param name="requestUserId" value="${requestUserId}"></c:param>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<div class="center1020">
	<c:choose>
		<c:when test="${empty requestUserId || requestUserId == form.userId}">
			<h2>${l.l('Properties')}</h2>
			<c:import url="/user/profile.do?action=settings"/>

			<h2>${l.l('Параметры (сохраняются сразу)')}</h2>
			<c:import url="/user/profile.do?action=settings&subAction=parameters&requestUserId=${requestUserId}"/>

			<%-- значение по-умолчанию должно быть таким же как и при обращении к данной опции на чтение!!! --%>
			<h2>${l.l('Опции интерфейса')}</h2>

			<html:form action="/user/profile">
				<input type="hidden" name="method" value="updatePersonalization"/>

				<table class="data">
					<tr>
						<td>${l.l('Параметр')}</td>
						<td width="100%">${l.l('Value')}</td>
					</tr>

					<tr>
						<td nowrap="nowrap">
							${l.l('Порядок объектов в буфере')}
						</td>
						<td>
							<c:set var="key" value="iface.buffer.behavior"/>
							<ui:combo-single hiddenName="${key}" value="${ctxUser.personalizationMap.get(key, '1')}" widthTextValue="200px">
								<jsp:attribute name="valuesHtml">
									<li value="1">${l.l('Последний объект сверху')}</li>
									<li value="2">${l.l('Сохранять порядок объектов')}</li>
								</jsp:attribute>
							</ui:combo-single>
						</td>
					</tr>

					<tr>
						<td nowrap="nowrap">
							${l.l('Максимальное число объектов в буфере')}
						</td>
						<td>
							<c:set var="key" value="iface.buffer.maxObjects"/>
							<input type="text" name="${key}" value="${ctxUser.personalizationMap.get(key, '15')}" size="10"/>
						</td>
					</tr>

					<tr>
						<td nowrap="nowrap">
							${l.l('Extend right process card area on scroll down')}
						</td>
						<td>
							<c:set var="key" value="iface.process.card.extend.right.on.scroll.down"/>
							<ui:combo-single hiddenName="${key}" value="${ctxUser.personalizationMap.get(key, '1')}" widthTextValue="200px">
								<jsp:attribute name="valuesHtml">
									<li value="1">${l.l('Yes')}</li>
									<li value="0">${l.l('No')}</li>
								</jsp:attribute>
							</ui:combo-single>
						</td>
					</tr>

					<plugin:include endpoint="user.profile.options.jsp"/>
				</table>

				<c:set var="configTextUiid" value="${u:uiid()}"/>
				<c:set var="saveCommand" value="$$.ajax.post(this).done(() => { alert('${l.l('Сохранено, для применения изменений перегрузите интерфейс нажатием Ctrl+F5')}') })"/>

				<div class="mt1">
					<button class="btn-grey" type="button" onclick="${saveCommand}">${l.l('Сохранить опции')}</button>
					<button class="btn-white ml2" type="button"
						onclick="$('#${configTextUiid}').toggle(); $(this).toggleClass(['btn-white', 'btn-blue'])" title="${l.l('Показать текст конфигурации опций')}">${l.l('Текст')}</button>
				</div>

				<div id="${configTextUiid}" style="display: none;">
					<h2>${l.l('Текст конфигурации опций')}</h2>

					<textarea style="width: 100%; height: 400px; resize: vertical;">${ctxUser.personalizationMap.getDataString().replace('&', '&amp;')}</textarea>

					<button class="btn-grey mt1 icon" type="button" name="reset" title="${l.l('Delete all the stored personalization options')}" onclick="
						if (!confirm('${l.l('Reset all the user personalizations?')}')) return;
						this.value = 1; ${saveCommand}">
						<i class="ti-eraser"></i>
						${l.l('Reset')}
					</button>
				</div>
			</html:form>
		</c:when>
		<c:otherwise>
			<div>
				<c:url var="url" value="/user/profile.do">
					<c:param name="method" value="settings"/>
					<c:param name="subAction" value="parameters"/>
					<c:param name="requestUserId" value="${requestUserId}"/>
				</c:url>
				<c:import url="${url}"/>
			</div>

			<button class="btn-grey mt1" type="button" onclick="$$.ajax.loadContent('${createUrl}', this);">${l.l('Послать персональную новость')}</button>
		</c:otherwise>
	</c:choose>
</div>

<shell:title>
	<jsp:attribute name="text">
		<c:set var="config" value="${ctxSetup.getConfig('org.bgerp.action.open.ProfileAction$Config')}"/>
		<c:if test="${config.isOpen(requestUserId)}">
			<a target='_blank' href='${config.url(requestUserId)}' title='${l.l('Open Interface')}'>O</a>
		</c:if>
		<span id='user_title_${requestUserId}' class='title'>${ctxUserMap[u:int(requestUserId)].title}</span>
	</jsp:attribute>
</shell:title>
<shell:state text=""/>
