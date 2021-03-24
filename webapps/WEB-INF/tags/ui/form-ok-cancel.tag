<%@ tag body-content="empty" pageEncoding="UTF-8" description="OK and cancel buttons, sending the current form"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="toPostNames" description="large parameters to be moved out of query string in POST body"%>
<%@ attribute name="styleClass" description="CSS classes to be applied to each of buttons"%>

<c:if test="${empty toPostNames}">
	<c:set var="toPostNames" value="[]"/>
</c:if>

<c:set var="loadReturn" value="$$.ajax.load('${form.returnUrl}', $$.shell.$content())"/>

<ui:button type="ok" styleClass="mr1 ${styleClass}"
	onclick="$$.ajax.post(this.form, {toPostNames: ${toPostNames}}).done(() => {${loadReturn}})"/>
<ui:button type="cancel" onclick="${loadReturn}" styleClass="${styleClass}"/>
