<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html:form action="/user/process" styleId="processQueueFilter" style="display: none;">
	<input type="hidden" name="action" value="queueShow"/>
	<input type="hidden" name="id" value="${queue.id}"/>
	<input type="hidden" name="page.size" value="-1"/>

	<%-- фильтр скрытый --%>
</html:form>