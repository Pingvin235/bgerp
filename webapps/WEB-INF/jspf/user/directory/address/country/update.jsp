<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="user/directory/address">
	<input type="hidden" name="action" value="addressUpdate"/>
	<html:hidden property="addressCountryId"/>
	<table style="width: 100%;" class="data">
		<tr>
			<td width="100">${l.l('Параметр')}</td>
			<td>Значение</td>
		</tr>
		<tr>
			<td>${l.l('ID')}</td>
			<td>${form.param['addressCountryId']}</td>
		</tr>
		<tr>
			<td>${l.l('Название')}</td>
			<td><html:text property="title" style="width: 100%"/></td>
		</tr>
		<%-- вроде нигде не используется
		<tr valign="top">
			<td>Конфигурация</td>
			<td><html:textarea property="config" rows="7" style="width: 100%"/></td>
		</tr>
		--%>
		<%@ include file="../edit_tr.jsp"%>
	</table>
</html:form>

<shell:state ltext="${l.l('Редактирование страны')}" help="kernel/setup.html#address"/>
