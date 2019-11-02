<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="userTitle"><b>Дома пользователя: ${form.response.data.userHouses.size()}</b></div>
					
<div>
	<c:set var="userHouses" value="${form.response.data.userHouses}"/>
	<select class="select" id="userHouses" multiple="multiple" size="27" style="width:100%;height:100%;">
		<c:forEach var="house" items="${userHouses}">
			<option value="${house.id}">${house.title}</option>
		</c:forEach>
	</select>
</div>
<div><b>всего квартир: ${form.response.data.flat}</b></div>