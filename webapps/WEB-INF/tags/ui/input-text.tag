<%@ tag body-content="empty" pageEncoding="UTF-8" description="Input field with reset and action icons"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="id input, если не указан - генерируется"%>
<%@ attribute name="name" description="имя input"%>
<%@ attribute name="value" description="текущее значение"%>
<%@ attribute name="size" description="size input"%>
<%@ attribute name="style" description="стиль input"%>
<%@ attribute name="styleClass" description="класс input"%>
<%@ attribute name="placeholder" description="placeholder input"%>
<%@ attribute name="title" description="title input"%>
<%@ attribute name="onSelect" description="JS, что выполнять по выбору значения"%>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<input id="${uiid}" type="text" name="${name}" placeholder="${placeholder}" title="${title}"
	style="${style}" class="${styleClass}" size="${size}" value="${value}"
	onkeypress="if (enterPressed(event)){ ${onSelect} }"/>

<script>
	$(function() {
		$$.ui.inputTextInit($('#${uiid}'), function() { ${onSelect} });
	})
</script>