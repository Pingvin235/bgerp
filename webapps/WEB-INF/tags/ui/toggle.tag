<%@ tag body-content="empty" pageEncoding="UTF-8" description="Toggle button" %>
<%@ include file="/WEB-INF/jspf/taglibs.jsp" %>

<%@ attribute name="id" description="id of the checkbox input, if not mentioned will be generated" %>
<%@ attribute name="name" description="checkbox input's name" %>
<%@ attribute name="value" type="java.lang.Boolean" description="checkbox input's value" %>
<%@ attribute name="styleClass" description="additional class for the button element" %>
<%@ attribute name="prefixText" description="label before toggle button" %>
<%@ attribute name="title" description="Optional outer div's title" %>
<%@ attribute name="textOn" description="text which appears when toggle button is ON" %>
<%@ attribute name="textOff" description="text which appears when toggle button is OFF" %>
<%@ attribute name="onChange" description="javascript, to be executed on change event" %>

<%@ attribute name="inputName" description="Deprecated 'name'"%>
<c:if test="${not empty inputName}">
	${log.warnd("Deprecated attribute 'inputName' was used in tag 'ui:toggle', change it to 'name'")}
	<c:if test="${empty name}">
		<c:set var="name" value="${inputName}"/>
	</c:if>
</c:if>

<c:choose>
	<c:when test="${not empty id}">
		<c:set var="uiid" value="${id}"/>
	</c:when>
	<c:otherwise>
		<c:set var="uiid" value="${u:uiid()}"/>
	</c:otherwise>
</c:choose>

<c:set var="textOn" value="${u.maskEmpty(textOn, 'ON')}"/>
<c:set var="textOff" value="${u.maskEmpty(textOff, 'OFF')}"/>

<c:if test="${not empty title}">
	<c:set var="title"> title="${title}"</c:set>
</c:if>

<div id="${uiid}" class="btn-toggle ${styleClass}"${title}>
	<c:if test="${not empty prefixText}">
		<div class="text-pref">${prefixText}</div>
	</c:if>
	<label>
		<input type="checkbox" name="${name}" onChange="${onChange}" ${u:checkedFromBool(value)}>
			<span class="toggle"
				<c:if test="${not empty textOn}"> data-before="${textOn}"</c:if>
				<c:if test="${not empty textOff}"> data-after="${textOff}"</c:if>
			>
			<span class="switch"></span>
		</span>
	</label>
</div>