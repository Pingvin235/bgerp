<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="id" value="${form.param.id}"/>
<c:set var="list" value="${form.response.data.list}"/>

<%@ include file="/WEB-INF/jspf/parameter_list.jsp"%>