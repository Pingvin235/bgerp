<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:page-control pageFormId="${formUiid}" nextCommand="; $$.ajax.loadContent(document.getElementById('${formUiid}'), this)"/>