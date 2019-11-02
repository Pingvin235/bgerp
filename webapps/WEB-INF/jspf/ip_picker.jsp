<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
  устаревший инклуд, вместо него использовать тег ui:ip!
  всё описание параметров в ip.tag 
--%>

<ui:ip selector="${selector}" editable="${editable}"/>