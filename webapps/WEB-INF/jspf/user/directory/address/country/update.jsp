<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/directory/address">
	<input type="hidden" name="method" value="addressUpdate"/>
	<html:hidden property="addressCountryId"/>
	<table class="data">
		<tr>
			<td width="100">${l.l('Параметр')}</td>
			<td>${l.l('Value')}</td>
		</tr>
		<tr>
			<td>ID</td>
			<td>${form.param['addressCountryId']}</td>
		</tr>
		<tr>
			<td>${l.l('Название')}</td>
			<td><html:text property="title" style="width: 100%"/></td>
		</tr>
		<%@ include file="../edit_tr.jsp"%>
	</table>
</html:form>

<shell:state text="${l.l('Редактор страны')}" help="kernel/setup.html#address"/>
