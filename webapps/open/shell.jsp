<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<title><%@ include file="/WEB-INF/jspf/version.jsp"%> : ${l.l('Открытый интерфейс')}</title>
	<%-- TODO: All the scripts are not needed here. --%>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>

	<style>
		#title {
			display: flex;
		}
		#title h1.title {
			white-space: nowrap;
			overflow: hidden;
			text-overflow: ellipsis;
		}
	</style>
	<script>
		// overwrite function from 'user' interface
		$$.shell.$content = () => $('#content');
	</script>
</head>

<body>
	<div id="title" class="mt1"><div class="status"><h1 class="title"></h1></div></div>
	<div id="content">
		<%-- shortcut for param --%>
		<u:set var="uri" scope="request"><%=org.bgerp.servlet.filter.OpenFilter.getRequestURI(request)%></u:set>

		<%@ include file="/WEB-INF/jspf/open/test/test.jsp"%>
		<%@ include file="/WEB-INF/jspf/open/profile/url.jsp"%>
		<%@ include file="/WEB-INF/jspf/open/process/url.jsp"%>

		<plugin:include endpoint="open.jsp"/>
	</div>
</body>
</html>