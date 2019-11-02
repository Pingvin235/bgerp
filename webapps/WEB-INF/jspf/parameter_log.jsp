<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="id" value="${form.param.id}"/>
<c:set var="log" value="${form.response.data.log}"/>
<c:set var="paramLinkId" value="${u:uiid()}"></c:set>
<c:set var="nextCommand" value="; openUrlToParent( formUrl( this.form ), $('#${paramLinkId}') )"/>

<h1>Лог изменений параметров</h1>

<html:form action="/user/parameter.do" style="width: 100%;"  styleId="${paramLinkId}">
	<table style="width: 100%;" id="${paramLinkId}">
		<tr><td>
			<button class="btn-white mb0.5" onclick="openUrlToParent( '${form.returnUrl}', $('#${paramLinkId}') ); return false;">Закрыть</button>
			<%@ include file="/WEB-INF/jspf/page_control.jsp"%>			
		</td></tr>
	</table>
	
	<input type="hidden" name="action" value="parameterLog"/>
	<input type="hidden" name="id" value="${form.id}"/>
	<html:hidden property="objectType"/>	
	<html:hidden property="returnUrl"/>
	<table id="${paramLinkId}" style="width:100%" class="data mt05">
		<tr>
			<td>Дата</td>
			<td>Пользователь</td>
			<td>Параметр</td>
			<td width="100%">Значение</td>
		</tr>
		<c:forEach var="logItem" items="${log}">
			<tr>
				<td nowrap="nowrap">${logItem.getDateFormatted()}</td>
				<td nowrap="nowrap">${ctxUserMap[logItem.userId].title}</td>
				<td nowrap="nowrap">${ctxParameterMap[logItem.paramId].title}</td>
				<td width="100%">${logItem.text}</td>
			</tr>
		</c:forEach>
	</table>
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>


