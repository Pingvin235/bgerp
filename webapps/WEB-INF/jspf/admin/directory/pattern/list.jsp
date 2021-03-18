<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ include file="/WEB-INF/jspf/admin/directory/directory.jsp"%>

<%@ include file="../create_button.jsp"%> 

<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="50%">${l.l('Название')}</td>
		<td width="50%">${l.l('Шаблон')}</td>
	</tr>
	<c:forEach var="item" items="${patternList}">
		<tr>
			<c:url var="editUrl" value="/admin/directory.do">
				<c:param name="action" value="patternTitleGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
				<c:param name="directoryId" value="${form.param.directoryId}"/>
			</c:url>
			
			<c:url var="deleteAjaxUrl" value="/admin/directory.do">
				<c:param name="action" value="patternTitleDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>
			
			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td align="right">${item.id}</td>
			<td>${item.title}</td>
			<td>${item.pattern}</td>
		</tr>
	</c:forEach>
</table>

<c:set var="state" value=""/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>