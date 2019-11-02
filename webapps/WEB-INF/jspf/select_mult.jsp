<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
  устаревший инклуд, вместо него использовать тег ui:select-mult!
  всё описание параметров в select-mult.tag 
--%>

<c:set var="showId" value="${not empty showId}"/>
<c:set var="showComment" value="${not empty showComment}"/>
<c:set var="moveOn" value="${not empty moveOn}"/>
<c:set var="fakeHide" value="${not empty fakeHide}"/>

<ui:select-mult
	id="${id}" hiddenName="${hiddenName}" values="${values}" 
	style="${style}" styleClass="${styleClass}"
	placeholder="${placeholder}" onSelect="${onSelect}"
	showId="${showId}" showComment="${showComment}" moveOn="${moveOn}" fakeHide="${fakeHide}"
	list="${list}" map="${map}" availableIdList="${availableIdList}" availableIdSet="${availableIdSet}"/>