<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<%-- Если к процессу был привязан единый договор --%>
	<c:when test="${not empty stepData.commonContract}">
		<table style="width: 100%;" class="data">
			<tr>
				<td>Единый договор</td>
				<td>Адрес</td>
			</tr>
			<tr>
				<td>${stepData.commonContract.formatedNumber}</td>
				<td>${stepData.commonContract.address.value}</td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>
		<c:if test="${not empty stepData.customerCommonContractList}">
			<table style="width: 100%;" class="data">
				<tr>
					<td>Единый договор</td>
					<td>Адрес</td>
				</tr>
				<c:forEach var="commonContract" items="${stepData.customerCommonContractList}">
					<tr>
						<td>
							<c:set var="linkCommonContractToProcess">
								if( deleteLinksWithType( 'process', ${process.id}, 'bgbilling-commonContract' ) &&
									addLink( 'process', ${process.id}, 'bgbilling-commonContract', ${commonContract.id}, '${commonContract.formatedNumber}' ) )
								{
									${reopenProcessEditorCode }
								}
							</c:set>
							<a href="#" onclick="${linkCommonContractToProcess}">${commonContract.formatedNumber}</a>
						</td>
						<td>${commonContract.address.value}</td>
					</tr>
				</c:forEach>
			</table>
		</c:if>

		<c:set var="createAndLinkCode">
			var result = sendAJAXCommandWithParams( '/user/plugin/bgbilling/commonContract.do?action=commonContractCreate', 
							{ 'customerId' : ${stepData.customer.id},
							  'id' : ${process.id},
							  'addressParamId' : ${stepData.step.addressParamId},
							  'addressParamPos' : 1 ,
							  'commonContractTitle' : $('#commonContractTitle').val()
							} );
			if( result )
			{
				var commonContractId = result.data.contract.id;
			
				if( deleteLinksWithType( 'process', ${process.id}, 'bgbilling-commonContract' ) &&
					addLink( 'process', ${process.id}, 'bgbilling-commonContract', commonContractId, result.data.contract.formatedNumber ) )
				{
					${reopenProcessEditorCode}
				}
			}
		</c:set>

		<c:choose>
			<c:when test="${not empty stepData.manualTitleInput}">
				<table class="searchResult" style="width: 100%;">
					<c:if test="${stepData.manualTitleInput}">
						<tr>
							<td align="center">Номер единого договора (БЕЗ КОДА ГОРОДА!!!): <input type="text" id="commonContractTitle" size="25"/></td>
						</tr>
					</c:if>
					<tr>
						<td width="100%"><input type="button" value="Создать новый единый договор" onclick="${createAndLinkCode}" style="width: 100%"/></td>
					</tr>
				</table>
			</c:when>
		</c:choose>
	</c:otherwise>
</c:choose>