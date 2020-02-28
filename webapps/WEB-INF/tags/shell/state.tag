<%@ tag body-content="empty" pageEncoding="UTF-8" description="Manipulations with shell's state area"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="moveSelector" description="CSS selector of moving element"%>
<%@ attribute name="text" description="Text for setting, HTML supported"%>
<%@ attribute name="help" description="Help link"%>

<script>
$(function () {
	const $state = $('#title > .status:visible > .wrap > .center');
	<c:choose>
		<c:when test="${not empty moveSelector}">
			$state.find(">div").remove();
			$('${moveSelector}').appendTo($state);
		</c:when>
		<c:otherwise>
			<c:if test="${not empty text}">
				<c:set var="state" value="${text.replaceAll('\\\\n', '')}"/>	
			</c:if>
			<c:if test="${not empty help}">
				<c:set var="state">${state}&nbsp;<a title='Помощь' target='_blank' href='${help}'>?</a></c:set>
			</c:if>
			$state.html("<h1 class='state'>${state}</h1>");
		</c:otherwise>
	</c:choose>
})
</script>
