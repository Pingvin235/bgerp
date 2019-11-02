<%@page import="ru.bgcrm.dao.process.ProcessDAO"%>
<%@ page contentType="text/html; charset=UTF-8"%>

<%
    pageContext.setAttribute("MODE_USER_CREATED", ProcessDAO.MODE_USER_CREATED);
	pageContext.setAttribute("MODE_USER_CLOSED", ProcessDAO.MODE_USER_CLOSED); 
	pageContext.setAttribute("MODE_USER_STATUS_CHANGED", ProcessDAO.MODE_USER_STATUS_CHANGED);
%>
