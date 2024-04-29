<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<form id="${uiid}" action="${form.httpRequestURI}">
	<input type="hidden" name="action" value="contractObjectParameterList"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}" />
	<input type="hidden" name="contractId" value="${form.param.contractId}" />
	<input type="hidden" name="objectId" value="${form.param.objectId}" />
	<input type="checkbox" name="showEmptyParameters" ${u:checkedFromBool(not empty form.param.showEmptyParameters)}
			onclick="$$.ajax.load(this.form, document.getElementById('${uiid}').parentElement);"/>
			Показать незаполненные параметры
</form>

<table class="data hl mt05">
	<tr>
		<td>ID</td>
		<td width="40%">Параметр</td>
		<td width="60%">Значение</td>
	</tr>

	<c:forEach var="parameter" items="${frd.parameterList}">
		<c:if test="${form.param.showEmptyParameters or not empty parameter.getValue()}">
			<tr>
				<td align="center">${parameter.parameterId}</td>
				<td nowrap="nowrap">${parameter.title}</td>
				<td>
					<c:set var="value" value="${empty parameter.getValue() ? 'не указан' : parameter.getValue()}"/>
					${value}
					<%-- editing wasn't implemented for all parameter types, because of deprecated contract objects functionality, the read-only mode is used
					<c:set var="viewEditDivId" value="${u:uiid()}"/>
					<div id="${viewEditDivId}">
						<c:url var="url" value="${form.httpRequestURI}">
							<c:param name="action" value="getObjectParameter"/>
							<c:param name="billingId" value="${form.param.billingId}"/>
							<c:param name="contractId" value="${form.param.contractId}"/>
							<c:param name="objectId" value="${form.param.objectId}"/>
							<c:param name="paramId" value="${parameter.parameterId}"/>
							<c:param name="returnUrl" value="${form.requestUrl}"/>
						</c:url>
						<a href="#" onclick="$$.ajax.load('${url}', $('#${viewEditDivId}'))">${value}</a>
					</div>
					--%>
				</td>
			</tr>
		</c:if>
	</c:forEach>
</table>