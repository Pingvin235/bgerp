<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<title><%@ include file="/WEB-INF/jspf/version.jsp"%> : Авторизация</title>		
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>
<body>
<table width="100%" height="80%">
		<tr>
			<td align="center">
				<h1 style="font-size: 300%; color: #909060;"><%@ include file="/WEB-INF/jspf/version.jsp"%></h1>
				<br/>
				<h2 style="font-size: 200%; color: #ff4020; margin: 30px;">Доступ запрещен!!!</h2>
			</td>
		</tr>	
</table>
</body>
</html>

<%--
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>BG-CRM v.2.0</title>
	<c:url var="cssUrl" value="/css/common.css"/><link type="text/css" rel="stylesheet" href="${cssUrl}"/>
	<c:url var="jsUrl" value="/js/common.js"/><script type="text/javascript" src="${jsUrl}"></script>
</head>
<body>
<table width="100%" height="80%"><tr><td align="center"><h1 style="font-size: 300%; color: #909060;">BG-CRM v.2.0</h1>
<-@ include file="/WEB-INF/jspf/select_intetface.jsp"-->
<h2 style="font-size: 200%; color: #ff4020; margin: 30px;">Доступ запрещен!!!</h2>
<c:url var="url" value="/login.do">
	<c:param name="action" value="logout"/>
</c:url> 
<a href="${url}">Выход</a>
</td></tr></table>		
</body>
</html>
 --%>