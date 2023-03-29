<%@ tag body-content="empty" pageEncoding="UTF-8" description="Link to open customer"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="Customer ID" required="true" type="java.lang.Integer"%>
<%@ attribute name="customer" description="Customer itself" type="ru.bgcrm.model.Customer"%>
<%@ attribute name="text" description="Optional link text, if not defined - used customer title"%>

<ui:when type="user">
	<a href="/user/customer#${id}" onclick="$$.customer.open(${id}); return false;"><%--
	--%><c:choose>
			<c:when test="${not empty text}">${u.escapeXml(text)}</c:when>
			<c:otherwise>${u.escapeXml(customer.title)}</c:otherwise>
		</c:choose><%--
	--%></a><%--
--%></ui:when>
<ui:when type="open">
	OPEN LINK NOT SUPPPORTED
</ui:when>