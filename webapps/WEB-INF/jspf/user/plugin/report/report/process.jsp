<%@ page import="java.util.Enumeration"%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="center1020">
	<html:form action="/user/plugin/report/report">
		<input type="hidden" name="action" value="get"/>
		<input type="hidden" name="reportId" value="${form.param.reportId}"/>
		<input type="hidden" name="forwardFile" value="${form.forwardFile}"/>
		
		<ui:combo-single hiddenName="type">
			<jsp:attribute name="valuesHtml">
				<li value="create">${l.l('Созданные')}</li>
				<li value="close">${l.l('Закрытые')}</li>
			</jsp:attribute>
		</ui:combo-single>
		
		c:
		<ui:date-time paramName="dateFrom" value="${form.param.dateFrom}"/>
		по:
		<ui:date-time paramName="dateTo" value="${form.param.dateFrom}"/>
		
		<%-- TODO: Make different upload options here. --%>
		
		<button type="button" class="btn-grey ml1" onclick="$$.ajax.load(this.form, $$.shell.$content())">=&gt;</button>
		
		<ui:page-control nextCommand="; $$.ajax.load(this.form, $$.shell.$content())"/>
	</html:form>
	
	<table class="data mt1">
		<tr>
			<td>ID</td>
			<td>${l.l('Тип')}</td>
			<td>${l.l('Пользователь')}</td>
			<td>${l.l('Описание')}</td>
		</tr>
		<c:forEach var="row" items="${form.response.data.list}">
			<tr>
				<td><ui:process-link id="${row[0]}"/></td>
				<td>${row[1].title}</td>
				<td><ui:user-link id="${row[2].id}"/></td>
				<td>${row[3]}</td>
			</tr>
		</c:forEach>
	</table>
</div>