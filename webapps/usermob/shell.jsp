<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<!DOCTYPE html>

<html>
	<head>
		<title><%@ include file="/WEB-INF/jspf/title.jsp"%></title>
		<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
	</head>

	<%
		pageContext.setAttribute("app", request.getParameter("app"));
	%>

	<script>
		$(function () {
			const $tabs = $("#content").tabs({ cache: true });

			<c:url var="url" value="process.do?method=queue"/>
			$tabs.tabs( "add", "${url}", "${l.l('Процессы')}" );

			<c:url var="url" value="exit.jsp"/>
			$tabs.tabs( "add", "${url}", "${l.l('Выход')}", "class='ml2'" );

			<c:if test="${'embedded' eq app}">
				$("#content > .ui-tabs-nav").hide();
				<c:set var="contentStyle" value="padding: 0"/>
			</c:if>

			$$.event.disable();
		});
	</script>

	<body>
		<div id="content" style="${contentStyle}">
			<ul></ul>
		</div>
	</body>
</html>
