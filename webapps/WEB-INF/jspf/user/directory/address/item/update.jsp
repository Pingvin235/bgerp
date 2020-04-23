<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="itemType" value="${form.param.selectTab}"/>
<c:set var="item" value="${form.response.data[itemType]}"/>

<html:form action="user/directory/address" onsubmit="return false;" >
	<input type="hidden" name="action" value="addressUpdate"/>
	<html:hidden property="selectTab"/>
	<html:hidden property="addressItemId"/>
	<html:hidden property="addressCityId" value="${item.addressCity.id}"/>
		
	<table style="width: 100%;" class="data">
		<tr>
			<td width="100">Параметр</td>
			<td>Значение</td>
		</tr>
		<tr>
			<td>ID</td>
			<td>${form.param.addressItemId}</td>
		</tr>
		<tr>
			<td>Страна</td>
			<td>${item.addressCity.addressCountry.title}</td>
		</tr>
		<tr>
			<td>Город</td>
			<td>${item.addressCity.title}</td>
		</tr>
		<tr>
			<td>Название</td>
			<td><html:text property="title" style="width: 100%" value="${item.title}"/></td>
		</tr>		
		<%@ include file="../edit_tr.jsp"%>
	</table>
</html:form>

<c:set var="state">
	<span class='title'>Редактирование  
		<c:choose>
			<c:when test="${itemType eq 'quarter'}">квартала</c:when>
			<c:when test="${itemType eq 'area'}">района</c:when>
			<c:otherwise>улицы</c:otherwise>
		</c:choose>	
	</span>
</c:set>
<c:set var="help" value="http://www.bgcrm.ru/doc/3.0/manual/kernel/setup.html#address"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>