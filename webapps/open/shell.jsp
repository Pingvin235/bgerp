<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<title><%@ include file="/WEB-INF/jspf/version.jsp"%> : ${l.l('Открытый интерфейс')}</title>
	<%-- TODO: All the scripts are not needed here. --%>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>

<body>
	<div id="title" class="mt1"><div class="status"><h1 class="title"></h1></div></div>
	<div id="content">
		<%-- shortcut for param --%>
		<c:set var="uri" scope="request"><%=request.getAttribute(org.bgerp.servlet.filter.OpenFilter.REQUEST_ATTRIBUTE_URI)%></c:set>

		<%@ include file="/WEB-INF/jspf/open/profile/url.jsp"%>

		<c:set var="endpoint" value="open.jsp"/>
		<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>
	</div>
</body>
</html>