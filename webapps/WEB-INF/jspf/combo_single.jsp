<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
  устаревший инклуд, вместо него использовать тег ui:combo-single!
  всё описание параметров в combo-single.tag 
--%>

<ui:combo-single 
	id="${id}" hiddenName="${hiddenName}" prefixText="${prefixText}" value="${value}" style="${style}" styleClass="${styleClass}"
	styleTextValue="${styleTextValue}" widthTextValue="${widthTextValue}" onSelect="${onSelect}" disable="${disable}"
	showFilter="${showFilter eq '1'}" 
	list="${list}" map="${map}" available="${available}">
	<jsp:attribute name="valuesHtml">${valuesHtml}</jsp:attribute>
</ui:combo-single>	
	
	  