<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
  устаревший инклуд, вместо него использовать тег ui:combo-check!
  всё описание параметров в combo-check.tag 
--%>

<ui:combo-check 
	id="${id}" paramName="${paramName}" prefixText="${prefixText}" values="${values}" 
	onChange="${onChange}"
	showFilter="${showFilter eq '1'}" 
	style="${style}" styleClass="${styleClass}"
	styleTextValue="${styleTextValue}" widthTextValue="${widthTextValue}"
	list="${list}" map="${map}" available="${available}">
	<jsp:attribute name="valuesHtml">${valuesHtml}</jsp:attribute>
</ui:combo-check>
