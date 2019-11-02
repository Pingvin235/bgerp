<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="user/directory/address">
	<input type="hidden" name="action" value="addressUpdate"/>
	<html:hidden property="addressCityId"/>
	<html:hidden property="addressCountryId"/>
	<table style="width: 100%;" class="data">
		<tr>
			<td width="100">Параметр</td>
			<td>Значение</td>
		</tr>
		<tr>
			<td>ID</td>
			<td>${form.param['addressCityId']}</td>
		</tr>
		<tr>
			<td>Страна</td>
			<td>${form.param['addressCountryTitle']}</td>
		</tr>
		<tr>
			<td>Название</td>
			<td><html:text property="title" style="width: 100%"/></td>
		</tr>
		<%-- вроде нигде не используется
		<tr valign="top" class="even">
			<td>Конфигурация</td>
			<td><html:textarea property="config" rows="7" style="width: 100%"/></td>
		</tr>
		 --%>
		<%@ include file="../edit_tr.jsp"%>
</table>
</html:form>

<c:set var="state">
	<span class='title'>Редактирование города</span>
</c:set>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/setup.html#address"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
