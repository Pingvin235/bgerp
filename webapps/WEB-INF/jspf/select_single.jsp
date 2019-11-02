<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
  устаревший инклуд, вместо него использовать тег ui:select-single!
  всё описание параметров в select-single.tag 
--%>
<c:set var="showId" value="${not empty showId}"/>
<c:set var="showType" value="${not empty showType}"/>
<c:set var="showComment" value="${not empty showComment}"/>

<ui:select-single 
	id="${id}" hiddenName="${hiddenName}" value="${value}" style="${style}" styleClass="${styleClass}"
	placeholder="${placeholder}" inputAttrs="${inputAttrs}" onSelect="${onSelect}"
	showId="${showId}" showType="${showTyp}" showComment="${showComment}"
	list="${list}" map="${map}" availableIdList="${availableIdList}" availableIdSet="${availableIdSet}"
	additionalSourceFilter="${additionalSourceFilter}"/>