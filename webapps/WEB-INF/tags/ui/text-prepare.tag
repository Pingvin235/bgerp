<%@ tag body-content="empty" pageEncoding="UTF-8" description="Preprocessing of text: inserting open links, links recognizing."%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="text" description="Initial text"%>

<ui:when type="user">
	<c:set var="text">${text.replaceAll("#(\\d+)", "<a onclick='\\$\\$.process.open($1); return false;' href='/user/process#$1'>$0</a>")}</c:set>
</ui:when>
<ui:when type="open">
	<c:set var="text">${text.replaceAll("#(\\d+)", "<a href='/open/process/$1'>$0</a>")}</c:set>
</ui:when>
${u:httpLinksToHtml(text)}