<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${uri.startsWith('/open/dispatch')}">
	<c:import url = "/open/plugin/dispatch/dispatch.do?method=dispatchList"/>
</c:if>