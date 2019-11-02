<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${not empty state}">
	<c:set var="state" value="${state.replaceAll( '\\\\n', '' )}"/>	
</c:if>
<c:if test="${not empty help}">
	<c:set var="state">${state}&nbsp;<a title='Помощь' target='_blank' href='${help}'>?</a></c:set>
</c:if>
<script>
	$(function (){
		var $state = $('#title > .status:visible > .wrap > .center');
		$state.html("<h1 class='state'>${state}</h1>");	
	})
</script>