<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="currentElement" value="document.getElementById('${uiid}').parentElement"/>

<c:url var="editUrl" value="${form.httpRequestURI}">
	<c:param name="method" value="getContractObject"/>
	<c:param name="billingId" value="${form.param.billingId }" />
	<c:param name="contractId" value="${form.param.contractId}" />
	<c:param name="returnUrl" value="${form.requestUrl}" />
</c:url>
<%--
contract objects are seem to be no longer supported, thus no adding functionality implemented
<ui:button type="add" styleClass="mb05" onclick="$$.ajax.load('${editUrl}', ${currentElement})"/>
--%>

<c:url var="deleteUrl" value="${form.httpRequestURI}">
	<c:param name="method" value="deleteContractObject"/>
	<c:param name="billingId" value="${form.param.billingId }" />
	<c:param name="contractId" value="${form.param.contractId}" />
</c:url>

<table id="${uiid}" class="data hl">
	<tr>
		<td></td>
		<td>ID</td>
		<td>Период</td>
		<td>Название</td>
		<td>Тип</td>
	</tr>
	<c:forEach var="object" items="${frd.objectList}">
		<tr>
			<td nowrap="nowrap">
				<c:url var="url" value="${editUrl}">
					<c:param name="objectId" value="${object.getId()}"/>
					<c:param name="objectType" value="${object.getType()}"/>
				</c:url>
				<ui:button type="edit" styleClass="btn-small" onclick="$$.ajax.load('${url}', ${currentElement})"/>

				<c:url var="url" value="${deleteUrl}">
					<c:param name="objectId" value="${object.getId()}"/>
				</c:url>
				<ui:button type="del" styleClass="btn-small" onclick="$$.ajax.post('${url}').done(() => $$.ajax.load('${form.requestUrl}', ${currentElement}))"/>
			</td>
			<td>${object.getId()}</td>
			<td nowrap="nowrap">${object.getPeriod()}</td>
			<td width="100%">${object.getTitle()}</td>
			<td nowrap="nowrap">${object.getType()}</td>
		</tr>
	</c:forEach>
</table>