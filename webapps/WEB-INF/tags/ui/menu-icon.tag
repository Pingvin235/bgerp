<%@ tag body-content="scriptless" pageEncoding="UTF-8" description="Menu icon"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="icon" description="Font icon"%>

<c:if test="${not empty icon}">
	<span class="${icon}"></span>
</c:if>