<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<title><%@ include file="/WEB-INF/jspf/version.jsp"%> : Открытый интерфейс</title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>

<body>
<%@ include file="/WEB-INF/jspf/error_dialog.jsp"%>

<div id="main">
<script>
$(function () {
	const href = window.location.href;

	let url = null; 
	let m = null;
		
	if (false) {}
	
	<c:set var="endpoint" value="open.jsp"/>
	<%@ include file="/WEB-INF/jspf/plugin_include.jsp"%>

	if (url) $$.ajax.load(url, $("#main"), {replace: true});
})
</script>
</div>
</body>
</html>