<%@ tag body-content="empty" pageEncoding="UTF-8" description="OK and cancel buttons, sending the current form"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="styleClass" description="CSS classes to be applied to each of buttons"%>
<%@ attribute name="loadReturn" description="load return JS command instead of standard one"%>

<c:if test="${empty loadReturn}">
	<c:set var="loadReturn" value="$$.ajax.loadContent('${form.returnUrl}', this)"/>
</c:if>

<ui:button type="ok" styleClass="mr1 ${styleClass}"
	onclick="$$.ajax.post(this.form).done(() => {${loadReturn}})"/>
<ui:button type="cancel" onclick="${loadReturn}" styleClass="${styleClass}"/>
