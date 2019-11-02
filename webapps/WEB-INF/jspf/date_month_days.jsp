<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
  устаревший инклуд, вместо него использовать тег ui:date-month-days!
  всё описание параметров в date-month-days.tag 
--%>

<ui:date-month-days 
	dateFromHiddenName="${dateFromHiddenName}" 
	dateToHiddenName="${dateToHiddenName}"/>