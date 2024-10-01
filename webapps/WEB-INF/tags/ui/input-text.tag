<%@ tag body-content="empty" pageEncoding="UTF-8" description="Input field with reset and action icons"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="input element id, auto generated if not explicitly specified"%>
<%@ attribute name="name" description="input element name"%>
<%@ attribute name="value" description="current value"%>
<%@ attribute name="size" description="input element size"%>
<%@ attribute name="style" description="input element's CSS style"%>
<%@ attribute name="styleClass" description="input element CSS class"%>
<%@ attribute name="placeholder" description="input element placeholder"%>
<%@ attribute name="title" description="input element title"%>
<%@ attribute name="onSelect" description="JS, action to be performed on value selection"%>
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

<div style="display: inline-block; position: relative;">
	<div style="display: flex;">
		<input id="${uiid}" type="text" name="${name}" placeholder="${placeholder}" title="${title}"
			style="${style}" class="${styleClass} w100p" size="${size}" value="${value}"
			onkeypress="if (enterPressed(event)){ $(this).parent().find('>button').click(); return false;};"/>
		<c:if test="${showOutButton}">
			<ui:button type="out" onclick="${onSelect}" styleClass="ml05"/>
		</c:if>
	</div>
</div>

<script>
	$(function() {
		$$.ui.inputTextInit($('#${uiid}'), ${showOutButton});
	})
</script>