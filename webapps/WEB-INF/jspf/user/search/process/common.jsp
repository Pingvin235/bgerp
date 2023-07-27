<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:page-control pageFormId="searchForm-process" nextCommand="; $$.ajax.load(document.getElementById('searchForm-process'), $('#searchResult'));" />
<%@ include file="../process_search_constants.jsp"%>
