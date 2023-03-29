<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="id" value="${u:uiid()}"/>
<div id="${id}_short">
	<c:choose>
		<c:when test="${text.length() gt maxLength}">
				${u:truncateHtml(text, maxLength)}...
				<a href="#" onclick="document.getElementById('${id}_short').style.display = 'none'; document.getElementById('${id}_full').style.display = 'block';">&gt;&gt;&gt;&gt;</a>
		</c:when>
		<c:otherwise>${text}</c:otherwise>
	</c:choose>
</div>
<div id="${id}_full" style="display: none;">
	${text}<br/><a href="#" onclick="document.getElementById('${id}_short').style.display = 'block'; document.getElementById('${id}_full').style.display = 'none';">&lt;&lt;&lt;&lt;</a>
</div>