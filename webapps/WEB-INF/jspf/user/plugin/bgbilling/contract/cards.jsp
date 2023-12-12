<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/bgbilling/proto/contract" onsubmit="return false;"  styleId="${uiid}">
	<input type="hidden" name="action" value="getContractCard"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>

	<ui:combo-single hiddenName="cardType" prefixText="Выберите карту:" widthTextValue="300px;">
		<jsp:attribute name="valuesHtml">
			<c:forEach var="item" items="${form.response.data.cardTypeList}">
				<li value="${item[0]}">${item[1]}</li>
			</c:forEach>
		</jsp:attribute>
	</ui:combo-single>

	<button type="button" class="btn-grey ml1" onclick="this.form.submit()">Сгенерировать</button>
</html:form>

<h2>Полная карта</h2>

<div>
	${form.response.data.fullCard.replaceAll('<style>[\\w\\s\\:\\{\\}\\-;]+</style>', '')}
</div>