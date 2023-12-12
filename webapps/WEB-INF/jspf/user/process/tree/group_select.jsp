<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty form.response.data.groups}">
	<ui:combo-single id="processGroups" list="${form.response.data.groups}" hiddenName="groupId" prefixText="${l.l('Группа')}:"/>
</c:if>