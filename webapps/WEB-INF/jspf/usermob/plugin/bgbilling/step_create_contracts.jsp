<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<div class="tableIndent">
		Созданные договора:
	</div>
	
	<table class="oddeven" style="width: 100%;">
		<c:choose>
			<c:when test="${not empty stepData.contractLinkList}">
				<c:forEach var="item" items="${stepData.contractLinkList}">
						<tr>
							<td>${item.linkedObjectTitle}</td>
						
							<c:set var="typeId" value="${item.configMap['typeId']}"/>
							<c:set var="tariffId" value="${item.configMap['tariffId']}"/>
							<c:set var="type" value="${stepData.step.typeMap[u:int(typeId)]}"/>
							<c:set var="tariffTitle" value="${type.tariffMap[u:int(tariffId)]}"/>
						
							<td>${type.title}</td>
							<td>${tariffTitle}</td>
						</tr>
				</c:forEach>
			</c:when>
			<c:otherwise>
				<tr><td>Договоров не создано.</td></tr>			
			</c:otherwise>
		</c:choose>
	</table>
	
	<jsp:useBean id="now" class="java.util.Date" scope="page"/>
	<fmt:formatDate value="${now}" pattern="dd.MM.yyyy" var="currentDate"/>
	
	<html:form action="/user/plugin/bgbilling/contract" style="width: 100%;">
		<input type="hidden" name="action" value="contractCreate"/>
		<input type="hidden" name="date" value="${currentDate}"/>
		<input type="hidden" name="customerId" value="${stepData.customer.id}"/>
		<input type="hidden" name="billingId"/>
		<input type="hidden" name="patternId"/>
		<input type="hidden" name="сomment" value="${stepData.customer.title}"/>
		
		<c:set var="usingCommonContract" value="${not empty stepData.commonContract}"/>
		<c:set var="contractTypesConfig" value="${stepData.step}"/>
				
		<c:set var="afterContractCreateCode">
			addLink( 'process', ${process.id}, type, contractId, contractTitle, { 'typeId' : typeId, 'tariffId' : tariffId } );
			${reopenProcessEditorCode}
		</c:set>
		
		<c:if test="${usingCommonContract}">
			<input type="hidden" name="commonContractId" value="${stepData.commonContract.id}"/>
			<input type="hidden" name="serviceCode"/>
		</c:if>	
		
		<table style="width: 100%;">
			<tr>
				<c:if test="${stepData.showContractTitle}">
					<td>
						<input type="text" name="title" style="width:100%"/>
					</td>
				</c:if>
				<td width="40%">
					<%@ include file="/WEB-INF/jspf/user/plugin/bgbilling/contract_create_code.jsp"%>
					
					<select style="width: 100%;" name="selectType" id="selectType" onchange="${typeChangedCode}">
						<option value="0">-- выберите тип --</option>
						<c:forEach var="item" items="${stepData.allowedTypeList}">
							<option value="${item.id}">${item.title}</option>
						</c:forEach>
					</select>
				</td>
				<td width="40%">				
					<select style="width: 100%;" name="tariffId" id="selectTariff">
						<%-- сюда динамически загружаются тарифы --%>
					</select>
				</td>
				<td>
					<input type="button" value="Создать" onclick="${contractCreateCode}"/>
				</td>
			</tr>		
		</table>		
	</html:form>
</div>