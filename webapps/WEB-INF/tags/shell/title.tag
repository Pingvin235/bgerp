<%@ tag body-content="empty" pageEncoding="UTF-8" description="Manipulations with shell's title area"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="text" description="Text for setting, HTML supported"%>

<c:if test="${not empty text}">
	<c:set var="title" value="${text.replaceAll('\\\\r', '').replaceAll('\\\\n', ' ')}"/>
</c:if>

<script>
	$(function () {
		$('#title > .status:visible h1.title').html("${title}");
	})
</script>
