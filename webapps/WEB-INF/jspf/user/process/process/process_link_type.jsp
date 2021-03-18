<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${type eq 'processLink'}"><span title="${l.l('Ссылается')}">[${l.l('С')}]</span></c:when>
	<c:when test="${type eq 'processDepend'}"><span title="${l.l('Зависит')}">[${l.l('З')}]</span></c:when>
	<c:when test="${type eq 'processMade'}"><span title="${l.l('Породил')}">[${l.l('П')}]</span></c:when>
</c:choose>