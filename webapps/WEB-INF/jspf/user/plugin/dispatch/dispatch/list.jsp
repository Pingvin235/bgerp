<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="showCode" value="openUrlContent( formUrl( $('#${uiid}') ) );"/>

<c:url var="getUrl" value="/user/plugin/dispatch/dispatch.do">
   <c:param name="action" value="dispatchGet"/>
   <c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<html:form action="/user/plugin/dispatch/dispatch" styleClass="in-mr1 in-mb1" styleId="${uiid}" style="vertical-align: middle;">
	<button type="button" class="btn-green" onclick="openUrlContent( '${getUrl}' )">+</button>

	<input type="hidden" name="action" value="dispatchList"/>

    <button class="btn-grey" type="button" onclick="${showCode}">${l.l('Вывести')}</button>

    <%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>${l.l('Наименование')}</td>
		<td>${l.l('Комментарий')}</td>
		<td>${l.l('Подписчиков')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
		 	<c:url var="editUrl" value="${getUrl}">
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/user/plugin/dispatch/dispatch.do">
				<c:param name="action" value="dispatchDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="${showCode}"/>

			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td>${item.id}</td>
			<td>${item.title}</td>
			<td>${item.comment}</td>
			<td>${item.accountCount}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="Рассылки"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>