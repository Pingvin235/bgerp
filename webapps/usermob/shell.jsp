<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
	<head>
		<title><%@ include file="/WEB-INF/jspf/version.jsp"%></title>
		<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
	</head>

	<%
		pageContext.setAttribute("app", request.getParameter("app"));
	%>

	<script>
		$(function ()  {
			var $tabs = $("#content").tabs( { cache : true } );

			<c:url var="url" value="/user/process/queue.do?action=queue"/>
			$tabs.tabs( "add", "${url}", "Процессы" );

			<c:url var="url" value="exit.jsp"/>
			$tabs.tabs( "add", "${url}", "Выход", "class='ml2'" );

			<c:if test="${'embedded' eq app}">
				$("#content > .ui-tabs-nav").hide();
				<c:set var="contentStyle" value="padding: 0"/>
			</c:if>
		});
	</script>

	<body>
		<div id="content" style="${contentStyle}">
			<ul></ul>
		</div>
	</body>
</html>
