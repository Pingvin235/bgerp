<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
  устаревший инклуд, вместо него использовать тег ui:date-time!
  всё описание параметров в date-time.tag 
--%>

<ui:date-time 
	paramName="${paramName}" value="${initialDate}" type="${type}" selector="${selector}" 
	editable="${editable}" saveCommand="${saveCommand}"/>