<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/process">
	<html:hidden property="action"/>
	<html:hidden property="from"/>
	<html:hidden property="open"/>
	
	<c:forEach var="item" items="${form.getSelectedValuesListStr('object')}">
		<input type="hidden" name="object" value="${item}"/>		
	</c:forEach>

	<c:set var="sendCommand">openUrlToParent( formUrl( $(this.form) ), $(this.form) )</c:set>

	<button type="button"
			onclick="this.form.open.value = this.form.open.value ? '' : 'true'; ${sendCommand}" 
			class="mr1 ${form.param.open eq 'true' ? 'btn-blue' : 'btn-white'}">${l.l('Только открытые')}</button>

	<c:set var="nextCommand" value="; ${sendCommand}"/>
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>		
</html:form>

<table class="data mt1" style="width: 100%;">
	<tr>
		<td>ID</td>
		<td>${l.l('Описание')}</td>
		<td>${l.l('Статус')}</td>
		<td>${l.l('Создан')}</td>
	</tr>
	
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td><a href="#" onclick="openProcess( ${item.id} ); return false;">${item.id}</a></td>
			<td>${item.description}</td>
			<td>${item.statusTitle}</td>
			<td>${u:formatDate( item.createTime, 'ymdhms' ) }</td>
		</tr>
	</c:forEach>
</table>