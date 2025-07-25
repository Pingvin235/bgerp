<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="createUrl" value="news.do">
	<c:param name="method" value="newsEdit"></c:param>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="showCode" value="$$.ajax.loadContent($('#${uiid}'), this);"/>

<div class="center1020">
	<html:form action="/user/news" onsubmit="return false;" styleClass="mb1 in-mr1" styleId="${uiid}">
		<input type="hidden" name="method" value="newsList"/>
		<ui:button type="add" onclick="$$.ajax.loadContent('${createUrl}', this);"/>

		<input type="text" size="30" onkeypress="if (enterPressed(event)) { ${showCode} }" placeholder="${l.l('Фильтр по тексту')}" name="text" value="${form.param['text']}" class="ml1"/>

		<ui:combo-single hiddenName="read" value="${form.param['read']}" widthTextValue="100px" onSelect="${showCode}">
			<jsp:attribute name="valuesHtml">
				<li value="0">${l.l('Unread')}</li>
				<li value="-1">${l.l('All')}</li>
				<li value="1">${l.l('Read')}</li>
			</jsp:attribute>
		</ui:combo-single>

		<button class="btn-grey" onclick="${showCode}">${l.l('Вывести')}</button>

		<ui:page-control/>
	</html:form>

	<c:forEach var="item" items="${frd.list}">
		<table class="data mb1">
			<tr>
				<td>
					<span class="tt">
						<c:choose>
							<c:when test="${item.read}">${item.title}</c:when>
							<c:otherwise><b>${item.title}</b></c:otherwise>
						</c:choose>
					</span>

					<span style="float:right;">
						${l.l('Создал')}: <ui:user-link id="${item.userId}"/>
						&nbsp;${tu.format( item.createDate, 'ymdhms')}
						&nbsp;<c:if test="${item.popup}">${l.l('Всплывающая')}</c:if>

						<%-- правка пока невозможна из-за персональных сообщений пользователям, поправить попозже, когда будут сообщения
						правка
						 <c:url var="editUrl" value="${createUrl }">
							<c:param name="id" value="${item.id}"/>
						</c:url>
						[ <a title="Править" href="#" onclick="$$.ajax.loadContent('${editUrl}'); return false;">${item.id}</a> ] --%>

						[ ${item.id} ]

						<c:url var="deleteUrl" value="news.do">
							<c:param name="method" value="newsDelete"></c:param>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						[ <a title="${l.l('Удалить')}" href="#" onclick="if (confirm('Удалить новость?')) $$.ajax.post('${deleteUrl}').done(() => { ${showCode} }); return false;">X</a> ]

						<c:if test="${not empty item.groupIds}">
							<br/>${l.l('Groups')}: ${u.getObjectTitles( ctxUserGroupList, item.groupIds ) }
						</c:if>
					</span>
				</td>
			</tr>
			<tr>
				<td class="mb05">
					<%-- Не используется u:htmlEncode, т.к. иначе сломается поддержка HTML! --%>
					<% pageContext.setAttribute("newLineChar", "\n"); %>
					${item.description.replace(newLineChar, "<br/>")}
				</td>
			</tr>
			<tr>
				<td style="text-align: right;">
					<c:choose>
						<c:when test="${item.read}">
							<button class="btn-white btn-small" onclick="$$.ajax.post('/user/news.do?method=newsSetRead&newsId=${item.id}&value=0').done(() => { ${showCode} })">${l.l('Unread')}</button>
						</c:when>
						<c:otherwise>
							<button class="btn-white btn-small" onclick="$$.ajax.post('/user/news.do?method=newsSetRead&newsId=${item.id}&value=1').done(() => { ${showCode} })">${l.l('Read')}</button>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</table>
	</c:forEach>

	<c:if test="${not empty frd.list and form.param.read ne 1}">
		<div style="text-align: right;">
			<button class="btn-grey mt1" onclick="$$.ajax.post('/user/news.do?method=newsSetAllRead').done(() => { ${showCode} })" title="${l.l('Пометить все новости прочитанными')}">${l.l('Все прочитаны')}</button>
		</div>
	</c:if>
</div>

<script>
	$(function()
	{
		clearInterval( bgcrm.blinkMessages );
		$('#messagesLink').attr('style','');
	})
</script>

<shell:title text="${l.l('News')}"/>
<shell:state help="kernel/news.html"/>
