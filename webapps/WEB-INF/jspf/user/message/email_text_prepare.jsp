<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<%
	String text = (String)pageContext.getAttribute( "text" );
	text = text.trim().replaceAll( "\r", "" ).replaceAll( "(\n\\s*){2,}", "\n\n" );
	pageContext.setAttribute( "text", text );
%>