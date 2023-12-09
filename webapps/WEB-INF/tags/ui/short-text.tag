<%@ tag body-content="empty" pageEncoding="UTF-8" description="Long text replaced by start of it and expanding link"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="text" description="the text" required="true"%>
<%@ attribute name="maxLength" type="java.lang.Integer" description="Maximal length to be shown, default 100 chars"%>

<c:if test="${empty maxLength}">
	<c:set var="maxLength" value="100"/>
</c:if>

<c:set var="id" value="${u:uiid()}"/>
<div id="${id}_short">
	<c:choose>
		<c:when test="${text.length() gt maxLength}">
				${u:truncateHtml(text, maxLength)}...
				<a href="#" onclick="document.getElementById('${id}_short').style.display = 'none'; document.getElementById('${id}_full').style.display = 'block'; return false;">&gt;&gt;&gt;&gt;</a>
		</c:when>
		<c:otherwise>${text}</c:otherwise>
	</c:choose>
</div>
<div id="${id}_full" style="display: none;">
	${text}<br/><a href="#" onclick="document.getElementById('${id}_short').style.display = 'block'; document.getElementById('${id}_full').style.display = 'none'; return false;">&lt;&lt;&lt;&lt;</a>
</div>