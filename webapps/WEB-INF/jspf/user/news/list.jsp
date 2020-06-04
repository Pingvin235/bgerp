<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="createUrl" value="news.do">
	<c:param name="action" value="newsEdit"></c:param>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="showCode" value="$$.ajax.load($('#${uiid}'), $$.shell.$content());"/>

<div class="center1020">
	<html:form action="user/news" onsubmit="return false;" styleClass="mb1 in-mr1" styleId="${uiid}">
		<input type="hidden" name="action" value="newsList"/>
		<button class="btn-green" onclick="$$.ajax.load('${createUrl}', $$.shell.$content());">+</button>

		<input type="text" size="30" onkeypress="if( enterPressed( event ) ){ ${showCode} }" placeholder="Фильтр по тексту" name="text" value="${form.param['text']}" class="ml1"/>

		<u:sc>
			<c:set var="valuesHtml">
				<li value="0">Непрочитанные</li>
				<li value="-1">Все</li>
				<li value="1">Прочитанные</li>
			</c:set>
			<c:set var="hiddenName" value="read"/>
			<c:set var="value" value="${form.param['read']}"/>
			<c:set var="onSelect" value="${showCode}"/>
			<c:set var="widthTextValue" value="100px"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
		</u:sc>

		<button class="btn-grey" onclick="${showCode}">Вывести</button>

		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	</html:form>

	<c:forEach var="item" items="${form.response.data.list}">
		<table class="data mb1" style="width: 100%;">
			<tr>
				<td>
					<span class="tt">
						<c:choose>
							<c:when test="${item.read}">${item.title}</c:when>
							<c:otherwise><b>${item.title}</b></c:otherwise>
						</c:choose>
					</span>

					<span style="float:right;">
						Создал: <ui:user-link id="${item.userId}"/>
						&nbsp;${u:formatDate( item.createDate, 'ymdhms')}
						&nbsp;<c:if test="${item.popup}">Всплывающая</c:if>

						<%-- правка пока невозможна из-за персональных сообщений пользователям, поправить попозже, когда будут сообщения
						правка
						 <c:url var="editUrl" value="${createUrl }">
							<c:param name="id" value="${item.id}"/>
						</c:url>
						[ <a title="Править" href="#UNDEF" onclick="openUrlContent( '${editUrl}' ); return false;">${item.id}</a> ] --%>

						[ ${item.id} ]

						<c:url var="deleteUrl" value="news.do">
							<c:param name="action" value="newsDelete"></c:param>
							<c:param name="id" value="${item.id}"/>
						</c:url>
						[ <a title="Удалить" href="#UNDEF" onclick="if( confirm( 'Удалить новость?' ) && sendAJAXCommand( '${deleteUrl}' ) ){ ${showCode} }; return false;">X</a> ]

						<c:if test="${not empty item.groupIds}">
							<br/>Группы: ${u:objectTitleList( ctxUserGroupList, item.groupIds ) }
						</c:if>
					</span>
				</td>
			</tr>
			<tr>
				<td class="mb05">
					<%-- Не используется u:htmlEncode, т.к. иначе сломается поддержка HTML! --%>
					<% pageContext.setAttribute("newLineChar", "\n"); %>
					${fn:replace(item.description, newLineChar, "<br/>")}
				</td>
			</tr>
			<tr>
				<td style="text-align: right;">
					<c:choose>
						<c:when test="${item.read}">
							<button class="btn-white btn-small" onclick="if( sendAJAXCommand( '/user/news.do?action=newsSetRead&newsId=${item.id}&value=0' ) ){ ${showCode} }">Не прочитано</button>
						</c:when>
						<c:otherwise>
							<button class="btn-white btn-small" onclick="if( sendAJAXCommand( '/user/news.do?action=newsSetRead&newsId=${item.id}&value=1' ) ){ ${showCode} }">Прочитано</button>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</table>
	</c:forEach>

	<c:if test="${not empty form.response.data.list and form.param.read ne 1}">
		<div style="text-align: right;">
			<button class="btn-grey mt1" onclick="if( sendAJAXCommand( '/user/news.do?action=newsSetAllRead' ) ){ ${showCode} };" title="Пометить все новости прочитанными">Все прочитаны</button>
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

<shell:title ltext="Новости"/>
<shell:state text="" help="kernel/news.html"/>