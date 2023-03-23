<%@ tag body-content="empty" pageEncoding="UTF-8" description="Toggle button" %>
<%@ include file="/WEB-INF/jspf/taglibs.jsp" %>

<%@ attribute name="id" description="id of the checkbox input, if not mentioned will be generated" %>
<%@ attribute name="inputName" description="name of input element" %>
<%@ attribute name="value" type="java.lang.Boolean" description="current value of the checkbox input" %>
<%@ attribute name="styleClass" description="additional class for the button element" %>
<%@ attribute name="prefixText" description="label before toggle button" %>
<%@ attribute name="textOn" description="text which appears when toggle button is ON" %>
<%@ attribute name="textOff" description="text which appears when toggle button is OFF" %>
<%@ attribute name="onChange" description="javascript, to be executed on change event" %>

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

<div id="${uiid}" class="btn-toggle ${styleClass}">
	<c:if test="${not empty prefixText}">
		<div class="text-pref">${prefixText}</div>
	</c:if>
	<label>
		<input type="checkbox" name="${inputName}" onChange="${onChange}" ${u:checkedFromBool(value)}>
			<span class="toggle"
				<c:if test="${not empty textOn}"> data-before="${textOn}"</c:if>
				<c:if test="${not empty textOff}"> data-after="${textOff}"</c:if>
			>
			<span class="switch"></span>
		</span>
	</label>
</div>