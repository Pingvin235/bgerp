<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="admin/process" styleClass="in-mr1">
	<input type="hidden" name="action" value="queueList"/>

	<c:url var="url" value="/admin/process.do">
		<c:param name="action" value="queueGet"/>
		<c:param name="id" value="-1"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
		
	<button type="button" class="btn-green" onclick="openUrlContent( '${url}' )">+</button>
	
	<ui:input-text styleClass="ml1" name="filter" value="${form.param.filter}" size="40" placeholder="Фильтр" title="Фильтр по наименованию, конфигурации"
		onSelect="openUrlContent( formUrl( this.form ) ); return false;"/>
		
	<button class="btn-grey" type="button" onclick="openUrlContent( formUrl( this.form ) )" title="${l.l('Вывести')}">=&gt;</button>
	
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>	


<table style="width: 100%;" class="data mt1">
	<tr>
		<td width="30">&#160;</td>
		<td width="30">ID</td>
		<td width="100%">${l.l('Наименование')}</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editUrl" value="/admin/process.do">
				<c:param name="action" value="queueGet"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="returnUrl" value="${form.requestUrl}"/>
			</c:url>
			<c:url var="deleteAjaxUrl" value="/admin/process.do">
				<c:param name="action" value="queueDelete"/>
				<c:param name="id" value="${item.id}"/>
			</c:url>
			<c:url var="deleteAjaxCommandAfter" value="openUrlContent( '${form.requestUrl}' )"/>
			
			<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td>${item.id}</td>
			<td>${item.title}</td>
		</tr>
	</c:forEach>
</table>

<shell:title ltext="Очереди процессов"/>
<shell:state text=""/>