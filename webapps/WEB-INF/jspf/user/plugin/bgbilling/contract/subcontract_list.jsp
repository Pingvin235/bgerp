<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty form.response.data.subContractList}">
	<h2>Договор является супердоговором для:</h2>
	<div style="overflow: auto; width: inherit; height: 610px;">
		<table class="data" style="width:100%;">
			<tr class="header">
				<td>№</td>
				<td>Название</td>
				<td></td>
			</tr>
			
			<c:forEach var="subContract" items="${form.response.data.subContractList}" varStatus="status">
				<tr>
					<td align="center" >${status.index+1}</td>
					<td width="100%">${subContract.getTitle() }</td>
					<td align="center" nowrap="nowrap">
						<input type="button" value="Открыть" onclick="bgbilling_openContract( '${form.param.billingId}', '${subContract.getId()}' ); return false;"/>
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</c:if>

<c:set var="supperContract" value="${form.response.data.superContract}"/>
<c:if test="${not empty supperContract}">
	<h2>Договор является субдоговором для: </br> ${supperContract.getTitle()} </h2>
	<input type="button" value="Открыть" onclick="bgbilling_openContract( '${form.param.billingId}', '${supperContract.getId()}' ); return false;"/>
</c:if>

<c:if test="${ empty supperContract and empty form.response.data.subContractList}">
	<h2>Договор независим</h2>
</c:if>