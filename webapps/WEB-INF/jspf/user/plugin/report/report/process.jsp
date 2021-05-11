<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%@ taglib tagdir="/WEB-INF/tags/plugin/report" prefix="report"%>

<shell:title ltext="Отчёт"/>
<shell:state ltext="Процессы"/>

<div class="report center1020">
	<html:form action="/user/plugin/report/report/process">
		<ui:combo-single hiddenName="type" widthTextValue="5em" value="${form.param.type}">
			<jsp:attribute name="valuesHtml">
				<li value="create">${l.l('Созданные')}</li>
				<li value="close">${l.l('Закрытые')}</li>
			</jsp:attribute>
		</ui:combo-single>
		&nbsp;
		${l.l("с")}:
		<ui:date-time paramName="dateFrom" value="${form.param.dateFrom}"/>
		${l.l("по")}:
		<ui:date-time paramName="dateTo" value="${form.param.dateTo}"/>
		
		<ui:button type="out" styleClass="ml1 mr1 more out" onclick="$$.ajax.load(this.form, $$.shell.$content(this))"/>

		<report:more data="${data}"/>

		<ui:page-control nextCommand="; $$.ajax.load(this.form, $$.shell.$content(this))" styleClass="more"/>
	</html:form>

	<%-- TODO: move to tag together with table --%>
	<div class="data mt1 w100p" style="overflow: auto;">
		<table class="data">
			<%-- TODO: columns and header showing tags --%>
			<tr>
				<td>ID</td>
				<td>${l.l('Тип')}</td>
				<td>${l.l('Пользователь')}</td>
				<td>${l.l('Время')}</td>
				<td>${l.l('Описание')}</td>
			</tr>
			<c:forEach var="r" items="${form.response.data.list}">
				<tr>
					<td><ui:process-link id="${r.get('id')}"/></td>
					<td>${r.get('type_title')}</td>
					<%-- u:int call is not needed here, just a sample of type convertion --%>
					<td><ui:user-link id="${u:int(r.get('user_id'))}"/></td>
					<td>${r.getString('time')}</td>
					<td>${r.get('process_description')}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</div>