<%@ tag body-content="empty" pageEncoding="UTF-8" description="Ссылка на открытие процесса"%> 
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%@ attribute name="id" description="Код процесса" required="true" type="java.lang.Integer"%>

<a href="/user/process#${id}" onclick="openProcess(${id}); return false;">${id}</a>
