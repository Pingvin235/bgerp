<%@ page contentType="text/html; charset=UTF-8"%><%-- 
--%><c:choose><%--
	--%><c:when test="${not empty setup['title']}"><%--
		--%>${setup['title']}<%--
	--%></c:when><%--
	--%><c:otherwise><%--
		--%>BGERP v.3.0<%--
	--%></c:otherwise><%--
--%></c:choose>