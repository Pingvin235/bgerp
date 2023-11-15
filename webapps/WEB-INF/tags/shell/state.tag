<%@ tag body-content="empty" pageEncoding="UTF-8" description="Manipulations with shell's state area"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="moveSelector" description="CSS selector of moved element"%>
<%@ attribute name="error" description="Plain text is shown as red text error when presented"%>
<%@ attribute name="text" description="Text for setting, HTML supported"%>
<%@ attribute name="help" description="Help link"%>

<c:if test="${not empty error}">
	<c:set var="stateUiid" value="${u:uiid()}"/>
	<h1 class="state" id="${stateUiid}" style="color: red;">${error}</h1>
	<c:set var="moveSelector" value="#${stateUiid}"/>
</c:if>

<script>
$(function () {
	const $state = $$.shell.$state();

	$state.html("");
	<c:if test="${not empty moveSelector}">
		$('${moveSelector}').appendTo($state);
	</c:if>
	<c:if test="${not empty text}">
		<c:set var="state" value="${text.replaceAll('\\\\n', '')}"/>
	</c:if>
	<c:if test="${not empty help}">
		<c:set var="state">${state}&nbsp;<a title='${l.l('Помощь')}' target='_blank' href='${u:docUrl(help)}'>?</a></c:set>
	</c:if>
	<c:if test="${not empty state}">
		$state.append("<h1 class='state'>${state}</h1>");
	</c:if>
})
</script>
