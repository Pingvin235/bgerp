<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="l" value="<%=org.bgerp.app.l10n.Localization.getLocalizer()%>"/>

<html>
<head>
	<title>${l.l('Interface')} | <%@ include file="/WEB-INF/jspf/title.jsp"%></title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>
<body>
<table width="100%" height="80%">
	<tr>
		<td align="center">
			<h1 style="font-size: 3em; color: #909060;"><%@ include file="/WEB-INF/jspf/title.jsp"%></h1>
			<%
				if (session != null) {
					session.invalidate();
				}
			%>
			<%@ include file="/WEB-INF/jspf/select_interface.jsp"%>
		</td>
	</tr>
</table>
</body>
</html>