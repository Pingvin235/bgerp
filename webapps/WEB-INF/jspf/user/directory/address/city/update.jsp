<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/directory/address">
	<input type="hidden" name="method" value="addressUpdate"/>
	<html:hidden property="addressCityId"/>
	<html:hidden property="addressCountryId"/>
	<table class="data">
		<tr>
			<td width="100">${l.l('Параметр')}</td>
			<td>${l.l('Value')}</td>
		</tr>
		<tr>
			<td>ID</td>
			<td>${form.param['addressCityId']}</td>
		</tr>
		<tr>
			<td>${l.l('Страна')}</td>
			<td>${form.param['addressCountryTitle']}</td>
		</tr>
		<tr>
			<td>${l.l('Название')}</td>
			<td><html:text property="title" style="width: 100%"/></td>
		</tr>
		<%@ include file="../edit_tr.jsp"%>
</table>
</html:form>

<shell:state text="${l.l('Редактор города')}" help="kernel/setup.html#address"/>

<c:if test="${not empty ctxParameterCache.getObjectTypeParameterList('address_city')}">
	<% out.flush(); %>
	<div>
		<h2>${l.l('ПАРАМЕТРЫ')}</h2>

		<div>
			<c:url var="url" value="/user/parameter.do">
				<c:param name="method" value="parameterList"/>
				<c:param name="id" value="${form.param.addressCityId}"/>
				<c:param name="objectType" value="address_city"/>
				<c:param name="parameterGroup" value="-1"/>
			</c:url>
			<c:import url="${url}"/>
		</div>
	</div>
</c:if>