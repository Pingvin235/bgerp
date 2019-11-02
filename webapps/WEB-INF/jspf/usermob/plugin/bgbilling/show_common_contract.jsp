<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="processId" value="${stepData.processId}"/>
<c:set var="commonContractList" value="${stepData.commonContractList}"/>
<c:set var="customerMap" value="${stepData.customerMap}"/>

<c:if test="${empty commonContractList}">
	Единых договоров не обнаружено!
</c:if>

<table class="data">
	<tr>
		<td>ЕД на данном адресе</td>
	</tr>
	<c:forEach var="contract" items="${commonContractList}">
		<tr>
			<c:set var="linkCode">
				<c:if test="${empty customerMap[contract.id]}">
					alert( 'На данном ЕД не обнаружено контрагента!' );
				</c:if>

				<c:if test="${not empty customerMap[contract.id]}">
					if( deleteLinksWithType( 'process', ${processId}, 'bgbilling-commonContract' ) 
						&& addLink( 'process', ${processId}, 'bgbilling-commonContract', ${contract.id}, '' ) )
					{
						<c:set var="addLink" value="true"/>
						<c:forEach var="processCustomerLink" items="${stepData.processCustomerLinkList}">
							<c:if test="${processCustomerLink.linkedObjectId == customerMap[contract.id].id}">
								<c:set var="addLink" value="false"/>
							</c:if>
						</c:forEach>

						<c:if test="${addLink}">
							addLink( 'process', ${processId}, 'customer', ${customerMap[contract.id].id}, '${customerMap[contract.id].title}' );
						</c:if>

						${reopenProcessEditorCode}
					}
				</c:if>
			</c:set>
			<td><a href="#UNDEF" onclick="${linkCode}">${contract.formatedNumber} [${customerMap[contract.id].title}]</a></td>
		</tr>
	</c:forEach>
</table>