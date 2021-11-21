<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="resultUiid" value="${u:uiid()}"/>

<form action="/open/plugin/subscription/subscription.do">
	<input type="hidden" name="action" value="calc"/>

	<ui:combo-single hiddenName="subscriptionId" widthTextValue="200px" list="${subscriptions}"/>

	<ui:combo-single hiddenName="limitId" prefixText="${l.l('Лимит')}:" styleClass="ml05" list="${limits}"/>

	<button class="btn-grey ml1" type="button" onclick="
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