<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

	<c:set var="houseIds" value="${u:getObjectIds(form.response.data.undistHouses)}"/>
<c:if test="${!empty houseIds}">
	<sql:query var="result" dataSource="${slaveDataSource}">
		SELECT sum(value)
		FROM address_config
		WHERE record_id IN ( ${houseIds} ) AND `key`='.i.flat.amount'
	</sql:query>

	<c:forEach var="row" items="${result.rowsByIndex}">
	<b>всего нераспределённых квартир: ${row[0]}</b>
	</c:forEach>
</c:if>