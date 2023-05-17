<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<!DOCTYPE html>

<html>
<head>
	<title>${l.l("Authorization")} | <%@ include file="/WEB-INF/jspf/title.jsp"%></title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>
<body>
	<script>
		$(function () {
			// if the page was loaded in internal element - reload
			if (document.getElementById("content")) {
				window.location.reload();
			}

			<c:if test="${ctxSetup.getBoolean('check.browser', true)}">
				<c:set var="firefoxVersion" value="22"/>
				<c:set var="chromeVersion" value="21"/>
				<c:set var="safariVersion" value="10"/>

				const userAgent = navigator.userAgent;
				if (userAgent.indexOf('Firefox') != -1 &&
					parseFloat(userAgent.substring(userAgent.indexOf('Firefox') + 8) ) >= ${firefoxVersion})
				{}
				else if (userAgent.indexOf('Chrome') != -1 &&
						 parseFloat(userAgent.substring(userAgent.indexOf('Chrome') + 7).split(' ')[0]) >= ${chromeVersion})
				{}
				else if (userAgent.indexOf('Safari') != -1 &&
						 parseFloat(userAgent.substring(userAgent.indexOf('Version') + 8).split('.')[0]) >= ${safariVersion})
				{}
				else {
					alert("${l.l('BGERP 3.0 не будет корректно работать в вашем браузере!')}\n" +
						  "${l.l('Поддерживаемые браузеры: Mozilla Firefox {} и новее, Google Chrome {} и новее, Safari {} и новее.', firefoxVersion, chromeVersion, safariVersion)}" );
				}
			</c:if>
		})
	</script>

	<%-- the form is re-created in login_dialog.jsp --%>
	<div style="position: absolute; top: 50%; margin-top: -100px;/* half of #content height*/ left: 0; width: 100%;">
		<form class="in-mt05" onsubmit="$$.shell.login.post().done(() => {
				window.location.reload();
			}); return false;"
			style="width: 250px; margin-left: auto; margin-right: auto; height: 200px; box-sizing: border-box;">
			<%@ include file="/WEB-INF/jspf/login_form_inputs.jsp"%>
		</form>
	</div>
</body>
</html>
