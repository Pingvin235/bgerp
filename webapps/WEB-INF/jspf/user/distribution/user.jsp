<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>


<u:sc>
	<c:set var="id" value="users"/>
	
	<c:set var="valuesHtml">
		<li value="-1">не выбран пользователь</li>
	</c:set>
	<c:set var="list" value="${form.response.data.users}"/>
	<c:set var="hiddenName" value="userId"/>
	<c:set var="prefixText" value="Пользователи:"/>
	<c:set var="onSelect">
		listUserHouses();
	</c:set>
	<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
</u:sc>