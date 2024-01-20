<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty frd.groups}">
	<ui:combo-single id="processGroups" list="${frd.groups}" hiddenName="groupId" prefixText="${l.l('Группа')}:"/>
</c:if>