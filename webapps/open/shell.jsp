<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<!DOCTYPE html>

<html>
<head>
	<title><%@ include file="/WEB-INF/jspf/title.jsp"%> : ${l.l('Открытый интерфейс')}</title>
	<%-- TODO: All the scripts are not needed here. --%>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
	<%@ include file="/WEB-INF/jspf/datepicker_l10n.jsp"%>

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

		<%@ include file="/WEB-INF/jspf/ui_init_js.jsp"%>
	</script>
</head>

<body>
	<div id="title" class="mt1"><div class="status"><h1 class="title"></h1></div></div>
	<div id="content">
		<% out.flush(); %>

		<%-- shortcut for param --%>
		<u:set var="uri" scope="request"><%=org.bgerp.app.servlet.filter.OpenFilter.getRequestURI(request)%></u:set>
		<u:set var="secret" scope="request"><%=org.bgerp.app.servlet.filter.OpenFilter.getRequestSecret(request)%></u:set>

		<%@ include file="/WEB-INF/jspf/open/test/url.jsp"%>
		<%@ include file="/WEB-INF/jspf/open/profile/url.jsp"%>
		<%@ include file="/WEB-INF/jspf/open/process/url.jsp"%>

		<plugin:include endpoint="open.jsp"/>
	</div>
</body>
</html>