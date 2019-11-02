<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="undistributedHouses"><b>Нераспределенные дома: ${form.response.data.undistHouses.size()}</b></div>

<div>
	<c:set var="undistHouses" value="${form.response.data.undistHouses}" />
	<select class="select" id="undistributedHouses" multiple="multiple" size="27" style="width:100%;height:100%;">
		<c:forEach var="house" items="${undistHouses}">
			<option value="${house.id}">${house.title}</option>
		</c:forEach>
	</select>
</div>
<div id="undistributedHouses">
<%@ include file="undistrictable_flat.jsp"%>
</div>

<%-- !${u:getObjectIds(form.response.data.undistHouses) }! --%>

