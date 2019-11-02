<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
	
<html:form action="/user/plugin/bgbilling/proto/contract" onsubmit="return false;"  styleId="${uiid}">
	<input type="hidden" name="action" value="getContractCard"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>
	
	<u:sc>
		<c:set var="valuesHtml">
			<c:forEach var="item" items="${form.response.data.cardTypeList}">
				<li value="${item[0]}">${item[1]}</li>
			</c:forEach>			
		</c:set>
		
		<c:set var="hiddenName" value="cardType"/>
		<c:set var="prefixText" value="Выберите карту:"/>
		<c:set var="widthTextValue" value="300px;"/>
		<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
	</u:sc>
	
	<button type="button" class="btn-grey ml1" onclick="this.form.submit()">Сгенерировать</button>
</html:form>

<h2>Полная карта</h2>

<div>
	${form.response.data.fullCard.replaceAll('<style>[\\w\\s\\:\\{\\}\\-;]+</style>', '')}
</div>