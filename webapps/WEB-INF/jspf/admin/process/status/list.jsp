<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="admin/process" styleClass="in-mr1">
	<input type="hidden" name="action" value="statusGet"/>
	<input type="hidden" name="id" value="-1"/>
	<input type="hidden" name="returnUrl" value="${form.requestUrl}"/>
	
	<button type="button" class="btn-green" onclick="openUrlContent( formUrl( this.form) )">+</button>
	
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td>Позиция</td>
		<td width="80%">Наименование</td>
		<td width="20%">Где используется</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/process.do">
				<c:param name="action" value="statusGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/process.do">
				<c:param name="action" value="statusDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>
			
			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			<td>${item.id}</td>
			<td>${item.pos}</td>
			<td>${item.title}</td>
			
			<c:set var="showId" value="${u:uiid()}" />
			<c:set var="statusId" value="${item.id}" />

			<td align="center">
				<c:url var="showUrl" value="/admin/process.do">
					<c:param name="action" value="statusUseProcess" />
					<c:param name="statusId" value="${statusId}" />
				</c:url> 							
								
				<button type="button" class="btn-white btn-small" style="width: 100%" onclick="$(this).hide(); openUrlTo('${showUrl}', $('#${showId}') );">Показать</button>
								
				<div id="${showId}">
				</div>
			</td>
		</tr>
	</c:forEach>
</table>

<c:set var="title" value="Статусы процессов"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/process/index.html#status"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>