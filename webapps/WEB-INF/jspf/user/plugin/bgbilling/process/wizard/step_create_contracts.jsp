<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<div>
		Созданные договора:
	</div>

	<table class="data hl" style="width: 100%;">
		<c:choose>
			<c:when test="${not empty stepData.contractLinkList}">
				<c:forEach var="item" items="${stepData.contractLinkList}">
						<tr>
							<td>${item.linkObjectTitle}</td>

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
	<c:set var="currentDate" value="${tu.format(now, 'dd.MM.yyyy')}"/>

	<c:set var="uiidForm" value="${u:uiid()}"/>

	<html:form action="/user/plugin/bgbilling/contract" styleId="${uiidForm}">
		<input type="hidden" name="method" value="contractCreate"/>
		<input type="hidden" name="date" value="${currentDate}"/>
		<input type="hidden" name="customerId" value="${stepData.customer.id}"/>
		<input type="hidden" name="comment" value="${stepData.customer.title}"/>

		<c:set var="afterContractCreateCode">
			addLink('process', ${process.id}, 'contract:' + this.form.billingId.value,
					result.data.contract.id, result.data.contract.title,
					{'typeId': this.form.typeId.value, 'tariffId': this.form.tariffId.value}).done(() => {
				${reopenProcessEditorCode}
			})
		</c:set>

		<c:set var="typeChangedCode" value="$$.bgbilling.contract.createTariff('${uiidForm}')"/>

		<table style="width: 100%;">
			<tr>
				<c:if test="${stepData.step.showContractTitle}">
					<td>
						<input type="text" name="title" style="width:100%"/>
					</td>
				</c:if>
				<td width="40%">
					<ui:combo-single name="typeId" onSelect="if (this.value > 0) { ${typeChangedCode} } else { $(this.form).find('#selectTariff').empty() }" style="width: 100%;">
						<jsp:attribute name="valuesHtml">
							<li value="0">-- выберите тип --</li>
							<c:forEach var="item" items="${stepData.allowedTypeList}">
								<li value="${item.id}">${item.title}</li>
							</c:forEach>
						</jsp:attribute>
					</ui:combo-single>
				</td>
				<td width="40%">
					<div id="selectTariff">
						<%-- сюда динамически загружаются тарифы --%>
					</div>
				</td>
				<td>
					<button type="button" class="btn-grey" onclick="
						if (!this.form.tariffId) { alert('Выберите тип договора.'); return; }
						$$.bgbilling.contract
							.create(this)
							.done((result) => { ${afterContractCreateCode} })
					">Создать</button>
				</td>
			</tr>
		</table>
	</html:form>
</div>