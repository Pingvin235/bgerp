<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h2>Отчёт за смену.</h2>

<c:set var="processTypeIds" value="${u.toString( processQueueMap[u:int(4)].processTypeIds )}"/>

<c:set var="openStatusId" value="10"/>
<c:set var="closeStatusId" value="8"/>
<c:set var="statusIds" value="${openStatusId},${closeStatusId}"/>
<c:set var="citiesParamId" value="48"/>

<html:form action="/user/empty">
	<input type="hidden" name="forwardFile" value="${form.forwardFile}"/>

	Смена с:
	<c:set var="uiid" value="${u:uiid()}"/>
	<html:text property="dateFrom" readonly="true" size="8" styleId="${uiid}"/>
	<c:set var="selector" value="#${uiid}"/>
	<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
	по:
	<c:set var="uiid" value="${u:uiid()}"/>
	<html:text property="dateTo" readonly="true" size="8" styleId="${uiid}"/>
	<c:set var="selector" value="#${uiid}"/>
	<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

	<html:select property="smena">
		<html:option value="1">Дневная</html:option>
		<html:option value="2">Ночная</html:option>
	</html:select>

	<input type="button" onclick="$$.ajax.load(this.form, $(this.form).parent())" value="Сформировать"/>
</html:form>

<c:set var="timeFrom" value="${tu.parse( form.param.dateFrom, 'ymd' ) }"/>
<c:set var="timeTo" value="${tu.parse( form.param.dateTo, 'ymd') }"/>
<c:set var="smena" value="${form.param.smena}"/>

<c:if test="${not empty timeFrom and not empty timeTo and smena gt 0}">
	<%-- дневная смена --%>
	<c:if test="${smena eq 1}">
		<c:set var="timeFrom" value="${u:convertDateToTimestamp( u:getDateHour( timeFrom, 9 ) ) }"/>
		<c:set var="timeTo" value="${u:convertDateToTimestamp( u:getDateHour( timeTo, 21 ) ) }"/>
	</c:if>
	<%-- ночная --%>
	<c:if test="${smena eq 2}">
		<c:set var="timeFrom" value="${u:convertDateToTimestamp( u:getDateHour( timeFrom, 21 ) ) }"/>
		<c:set var="timeTo" value="${u:convertDateToTimestamp( u:getDateHour( timeTo, 9 ) ) }"/>
	</c:if>

	<sql:query var="result" dataSource="${dataSource}">
		SELECT process.id, process.priority, process.description, status.title, DATE_FORMAT(process.create_dt, '%Y-%m-%d %H.%i.%s'), DATE_FORMAT(process.close_dt, '%Y-%m-%d %H.%i.%s'),
			process.status_id, city.id, city.title
		FROM process
		LEFT JOIN process_status_title AS status ON process.status_id=status.id
		LEFT JOIN param_list ON process.id=param_list.id AND param_list.param_id=?
		LEFT JOIN address_city AS city ON param_list.value=city.id
		WHERE process.type_id IN (${processTypeIds}) AND process.status_id IN (${statusIds})
			AND ((create_dt>=? AND create_dt<?) OR (close_dt>=? AND close_dt<?))
		GROUP BY city.id
		ORDER BY city.title

		<sql:param value="${citiesParamId}"/>
		<sql:param value="${timeFrom}"/>
		<sql:param value="${timeTo}"/>
		<sql:param value="${timeFrom}"/>
		<sql:param value="${timeTo}"/>
	</sql:query>

	<c:set var="lastCity" value=""/>

	<table style="width: 100%;">
		<c:forEach var="row" items="${result.rowsByIndex}">
			<c:set var="processId" value="${row[0]}"/>
			<c:set var="statusId" value="${row[6]}"/>
			<c:set var="cityTitle" value="${row[8]}"/>

			<c:if test="${lastCity ne cityTitle}">
				<tr><td colspan="5"></td></tr>
				<tr>
					<td colspan="5"><h2>Город ${cityTitle}</h2></td>
				</td>
				<tr>
					<td>ID</td>
					<td>Приоритет</td>
					<td>Описание</td>
					<td>Статус</td>
					<td>Время</td>
				</tr>
				<c:set var="lastCity" value="${cityTitle}"/>
			</c:if>

			<tr>
				<td>${processId}</td>
				<td>${row[1]}</td>
				<td>${row[2]}</td>
				<td>${row[3]}</td>
				<td>
					<c:if test="${statusId eq openStatusId}">
						${row[4]}
					</c:if>
					<c:if test="${statusId eq closeStatusId}">
						${row[5]}
					</c:if>
				</td>
			</tr>
			<tr>
				<td colspan="5">
					<c:url var="url" value="/user/link.do" >
						<c:param name="action" value="linkList"/>
						<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/crm/billing_problems.jsp"/>
						<c:param name="objectType" value="process"/>
						<c:param name="id" value="${processId}"/>
						<c:param name="linkedObjectType" value="bgbilling-problem"/>
						<c:param name="showOnly" value="1"/>
					</c:url>
					<c:import url="${url}"/>
				</td>
			</tr>
		</c:forEach>
</table>
</c:if>