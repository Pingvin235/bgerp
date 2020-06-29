<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="objectInfo" value="${form.param.billingId}-${form.param.contractId}-objectInfo"/>

<div id="${objectInfo}">
	<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
		<c:param name="action" value="addContractObject"/>
		<c:param name="billingId" value="${form.param.billingId }" />
		<c:param name="contractId" value="${form.param.contractId}" />
		<c:param name="returnUrl" value="${form.requestUrl}" />
	</c:url>
	<button type="button" class="btn-green" value="Добавить объект" onclick="alert('Функционал скоро будет')">+</button>
	
	<table class="data mt1" width="100%">
		<tr class="header">
			<td></td>
			<td>ID</td>
			<td>Период</td>
			<td>Название</td>
			<td>Тип</td>
		</tr>
		
		<c:forEach var="object" items="${form.response.data.objectList}">
				<tr>
					<c:url var="url" value="/user/plugin/bgbilling/proto/contract.do">
						<c:param name="action" value="getContractObject"/>
						<c:param name="billingId" value="${form.param.billingId }" />
						<c:param name="contractId" value="${form.param.contractId}" />
						<c:param name="objectId" value="${object.getId()}"/>
						<c:param name="objectType" value="${object.getType()}"/>
						<c:param name="returnUrl" value="${form.requestUrl}" />
					</c:url>
					<c:set var="editCommand" value="openUrlTo('${url}', $('#${objectInfo}') )"/>
					
					<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/contract.do">
						<c:param name="action" value="deleteContractObject"/>
						<c:param name="billingId" value="${form.param.billingId }" />
						<c:param name="contractId" value="${form.param.contractId}" />
						<c:param name="objectId" value="${object.getId()}"/>
					</c:url>
					<c:set var="deleteAjaxCommandAfter" value="openUrlTo('${form.requestUrl}',$('#${objectInfo}'))"/>
					<td nowrap="nowrap">
						<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
					</td>
					<td>${object.getId()}</td>
					<td nowrap="nowrap">${object.getPeriod()}</td>
					<td width="100%">${object.getTitle()}</td>
					<td nowrap="nowrap">${object.getType()}</td>
				</tr>
		</c:forEach>
	</table>
</div>