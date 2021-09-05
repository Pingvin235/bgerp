<%@ tag body-content="empty" pageEncoding="UTF-8" description="Manipulations with shell's state area"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="moveSelector" description="CSS selector of moving element"%>
<%@ attribute name="ltext" description="Plain text, previously localized"%>
<%@ attribute name="text" description="Text for setting, HTML supported"%>
<%@ attribute name="help" description="Help link"%>

<c:if test="${not empty ltext}">
	<c:set var="text" value="${l.l(ltext)}"/>
</c:if>

<script>
$(function () {
	const $state = $('#title > .status:visible > .wrap > .center');
	<c:choose>
		<c:when test="${not empty moveSelector}">
			$state.html("");
			$('${moveSelector}').appendTo($state);
		</c:when>
		<c:otherwise>
			<c:if test="${not empty text}">
				<c:set var="state" value="${text.replaceAll('\\\\n', '')}"/>
			</c:if>
			<c:if test="${not empty help}">
				<c:if test="${not help.startsWith('http')}">
					<c:set var="help">https://bgerp.org/doc/3.0/manual/${help}</c:set>
				</c:if>
				<c:set var="state">${state}&nbsp;<a title='${l.l('Помощь')}' target='_blank' href='${help}'>?</a></c:set>
			</c:if>
			$state.html("<h1 class='state'>${state}</h1>");
		</c:otherwise>
	</c:choose>
})
</script>
