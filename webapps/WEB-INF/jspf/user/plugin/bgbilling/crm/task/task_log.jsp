<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="box" style="overflow: auto; width: inherit; height: 240px;">
	<table>
		<tr height="40">
			<td><b>Открыта:</b></td>
			<td>${form.param.logOpen}</td>
		</tr>
		<tr height="40">
			<td><b>Принята:</b></td>
			<td>${form.param.logAccept}</td>
		</tr>
		<tr height="40">
			<td><b>Закрыта:</b></td>
			<td>${form.param.logClose}</td>
		</tr>
		<tr height="40">
			<td><b>Изменена:</b></td>
			<td>${form.param.logLastModify}</td>
		</tr>
	</table>
</div>