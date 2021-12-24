<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l("Планировщик")}</h1>

<table class="data" id="scheduler-tasks">
	<tr>
		<td>ID</td>
		<td>${l.l("Плагин")}</td>
		<td>${l.l("Наименование")}</td>
		<td>Java Class</td>
		<td>${l.l("Последний запуск")}</td>
		<td>&nbsp;</td>
	</tr>
	<c:forEach var="task" items="${form.response.data.tasks}">
		<tr id="${task.id}">
			<td>${task.id}</td>
			<td>${task.plugin.id}</td>
			<td>${task.className}</td>
			<td>${tu.format(task.lastStart, "ymdhms")}</td>
			<td>
				<ui:button type="run"/>
			</td>
		</tr>
	</c:forEach>
</table>