<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="pageFormId" value="${formUiid}" scope="request"/>
<c:set var="nextCommand" value="; openUrlContent( formUrl( $( '#${formUiid}' )[0] ) );" scope="request"/>
<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
