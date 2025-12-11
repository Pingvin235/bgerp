<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="resultUiid" value="${u:uiid()}"/>

<form action="${form.requestURI}">
	<input type="hidden" name="method" value="calc"/>

	<c:choose>
		<c:when test="${subscriptionId gt 0}">
			<input type="hidden" name="subscriptionId" value="${subscriptionId}"/>
		</c:when>
		<c:otherwise>
			<ui:combo-single name="subscriptionId" widthTextValue="20em" list="${subscriptions}" styleClass="mr05"/>
		</c:otherwise>
	</c:choose>

	<ui:combo-single name="limitId" prefixText="${l.l('Лимит')}:" styleClass="mr1" list="${limits}"/>

	<button class="btn-grey" type="button" onclick="
		const processIds = getCheckedProcessIds();
		if (!processIds) {
			alert('${l.l('Выберите продукты!')}');
			return;
		}
		$$.ajax.load($$.ajax.formUrl(this.form) + '&processIds=' + processIds, $('#${resultUiid}'));
	">${l.l('Посчитать')}</button>
</form>
<div id="${resultUiid}" class="in-inline-block">
	<%-- calculation result will be put here --%>
</div>