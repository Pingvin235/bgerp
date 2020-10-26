<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:if test="${empty toPostNames}">
	<c:set var="toPostNames" value="[]"/>
</c:if>

<button type="button" class="btn-grey mr1" onclick="if( sendAJAXCommand( formUrl( this.form ), ${toPostNames} ) ){ openUrlContent( '${form.returnUrl}' ) }">${l.l('ОК')}</button>
<button type="button" class="btn-grey" onclick="openUrlContent( '${form.returnUrl}' )">${l.l('Отмена')}</button>

<c:remove var="toPostNames"/>