<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%-- 
	Deprecated include, use directly tag ui:page-control. 
--%>
<ui:page-control pageFormSelectorFunc="${pageFormSelectorFunc}" 
				pageFormSelector="${pageFormSelector}" 
				pageFormId="${pageFormId}"
				nextCommand="${nextCommand}"/>
