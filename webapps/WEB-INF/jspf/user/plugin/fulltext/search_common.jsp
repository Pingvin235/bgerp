<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="pageFormId" value="searchForm-customer" scope="request"/>
<c:set var="nextCommand" value="; openUrl( formUrl( $( '#searchForm-fulltext' )[0] ), '#searchResult');" scope="request"/>
<%@ include file="/WEB-INF/jspf/page_control.jsp"%>