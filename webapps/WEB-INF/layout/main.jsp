<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<title><tiles:insert attribute="title"/></title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>

<body>
<table width="100%" height="100%" class="nopad">
	<tr><td height="100%" valign="top"><tiles:insert attribute="body"/></td></tr>
	<tr>
		<td align="right" valign="bottom">
			<tiles:insert attribute="footer"/>
		</td>
	</tr>
</table>
</body>
</html>