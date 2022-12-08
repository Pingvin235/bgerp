<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:page-control pageFormId="searchForm-fulltext" nextCommand="; $$.ajax.load($('#searchForm-fulltext'), $('#searchResult'));" />
