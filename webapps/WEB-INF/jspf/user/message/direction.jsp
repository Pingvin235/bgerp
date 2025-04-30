<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${message.incoming}"><i class="ti-cloud-down" title="${l.l('call.in')}"></i></c:when>
	<c:otherwise><i class="ti-cloud-up" title="${l.l('call.out')}"></i></c:otherwise>
</c:choose>
