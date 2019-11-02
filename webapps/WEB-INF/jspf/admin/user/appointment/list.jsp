<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:url var="createUrl" value="/admin/appointment.do">
		<c:param name="action" value="appointmentGet" />
		<c:param name="id" value="-1" />
		<c:param name="returnUrl" value="${form.requestUrl}" />
</c:url>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="showCode" value="openUrlContent( formUrl( $('#${uiid}') ) );"/>

<html:form action="admin/appointment" onsubmit="return false;" styleClass="mb1 in-mr1" styleId="${uiid}">
	<input type="hidden" name="action" value="appointmentList"/>
	<button type="button" class="btn-green" onclick="openUrlContent( '${createUrl}' )">+</button>
	<ui:input-text size="30" onSelect="${showCode}" name="title" value="${form.param['title']}" placeholder="Фильтр" styleClass="ml1"/>
	<button type="button" class="btn-grey" onclick="${showCode}">=&gt;</button>		
	
	<div style="display: inline-block; float: right; vertical-align: middle;" class="pt05 pb05">
    	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
    </div>
</html:form>

<table style="width:100%" class="data">
	<tbody>	
		<tr>
			<td width="30">&#160;</td>
			<td width="30">ID</td>		
			<td width="20%">Наименование</td>		
			<td width="80%">Комментарий</td>
		</tr>
	
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/appointment.do">
				<c:param name="action" value="appointmentGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/appointment.do">
				<c:param name="action" value="appointmentDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			
			<c:url var="deleteAjaxCommandAfter" value="${showCode}"/>
				
			<td nowrap><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td>${item.id}</td>
			<td>${item.title}</td>
			<td>${item.description}</td>			
		</tr>		
	</c:forEach>
	</tbody>	
</table>

<c:set var="title" value="Должности"/>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>