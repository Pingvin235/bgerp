<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="pageFormId" value="searchForm-fulltext" scope="request"/>
<c:set var="nextCommand" value="; $$.ajax.load($('#searchForm-fulltext')[0], $('#searchResult'));" scope="request"/>
<%@ include file="/WEB-INF/jspf/page_control.jsp"%>