<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="pageFormId" value="linkStreetForm"/>
<%@ include file="/WEB-INF/jspf/page_control.jsp"%>

<table width="100%" class="data" name="linkStreetList">
	<tr>
		<td></td>
		<td>Город</td>
		<td>crm_id</td>
		<td>Название в nCRM</td>
		<td>fias_id</td>
		<td>Название в ФИАС</td>
		<td>Действия</td>
	</tr>
	<c:forEach var="street" items="${form.response.data.list}">
		<tr>
			<td align="center">
				<c:url var="deleteAjaxUrl" value="plugin/fias.do">
					<c:param name="action" value="delStreetLink"/>
					<c:param name="streetId" value="${street.id}"/>
				</c:url>
				<c:set var="deleteAjaxCommandAfter" value="fias_clearStreetInput();"/>
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td align="center" nowrap="nowrap">${street.crmCitytitle}</td>
			<td align="center">${street.crmStreetId}</td>
			<td align="center" nowrap="nowrap" width="40%">${street.crmStreetTitle}</td>
			<td align="center" nowrap="nowrap">${street.id}</td>
			<td align="center" width="40%">${street.title} (${street.shortName})</td>
			<td>
				<c:url var="copyTitleUrl" value="plugin/fias.do">
					<c:param name="action" value="copyStreetTitle"/>
					<c:param name="streetId" value="${street.id}"/>
				</c:url>
				<input type="button" value="Скопировать название" 
					onclick="if( confirm( 'Вы уверены, что хотите скопировать название?' ) && sendAJAXCommand('${copyTitleUrl}')) { 
					alert('Название скопированно.'); openUrlTo(formUrl($('#linkStreetForm')),$('#linkStreetForm').next())}"/>
			</td>
		</tr>
	</c:forEach>
</table>