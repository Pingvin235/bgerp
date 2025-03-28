<%@ tag body-content="empty" pageEncoding="UTF-8" description="Input field for decimal numbers"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="input element id, auto generated if not explicitly specified"%>
<%@ attribute name="name" description="input element name"%>
<%@ attribute name="digits" type="java.lang.Integer" description="amount of digits after dot, if not defined than 2"%>
<%@ attribute name="value" description="current value"%>
<%@ attribute name="size" description="input element size"%>
<%@ attribute name="style" description="input element's CSS style"%>
<%@ attribute name="styleClass" description="input element CSS class"%>
<%@ attribute name="placeholder" description="input element placeholder"%>
<%@ attribute name="title" description="input element title"%>

<c:set var="uiid" value="${empty id ? u:uiid() : id}"/>
<c:set var="digits" value="${empty digits ? 2 : digits}"/>

<input id="${uiid}" type="text" name="${name}" autocomplete="off" placeholder="${placeholder}" title="${title}"
	style="${style}" class="${styleClass}" size="${size}" value="${value}"
	onkeydown="return $$.ui.input.decimal.onkeydown(event, ${digits})" onpaste="return $$.ui.input.decimal.onpaste(event, ${digits})"/>
