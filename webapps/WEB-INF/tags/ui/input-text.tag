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
<%@ attribute name="showOutButton" type="java.lang.Boolean" description="Show out button, default is 'true'"%>

<c:set var="showOutButton" value="${(empty showOutButton) ? true : showOutButton}" />

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<div style="display: inline-block;">
	<div style="display: flex;">
		<input id="${uiid}" type="text" name="${name}" placeholder="${placeholder}" title="${title}"
			style="${style}" class="${styleClass} w100p" size="${size}" value="${value}"
			onkeypress="if (enterPressed(event)){ ${onSelect}; return false;};"/>
		<c:if test="${showOutButton}">
			<ui:button type="out" onclick="${onSelect}" styleClass="ml05"/>
		</c:if>
	</div>
</div>

<script>
	$(function() {
		$$.ui.inputTextInit($('#${uiid}'));
	})
</script>