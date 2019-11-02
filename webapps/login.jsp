<%@page import="ru.bgcrm.util.Setup"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<html>
<head>
	<c:set var="setup" value="<%=Setup.getSetup()%>"/>
	<title><%@ include file="/WEB-INF/jspf/version.jsp"%> : ${l.l("Авторизация")}</title>
	<%@ include file="/WEB-INF/jspf/script_css.jsp"%>
</head>
<body>
	<%-- если окно авторизации открылось где-то в div-ке, после инициализации главных табов - редирект --%>
	<script>
		$(function()
		{		
			if( document.getElementById( "content" ) )
			{
				window.location.reload();
			}
			
			<c:if test="${setup.getBoolean('check.browser', true)}">
				<c:set var="firefoxVersion" value="22"/>
				<c:set var="chromeVersion" value="21"/>
				<c:set var="safariVersion" value="10"/>
				
				var userAgent = navigator.userAgent; 
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
					alert( "${l.l('BGERP 3.0 не будет корректно работать в вашем браузере!')}\n" +
						   "${l.l('Поддерживаемые браузеры: Mozilla Firefox %s и новее, Google Chrome %s и новее, Safari %s и новее.', firefoxVersion, chromeVersion, safariVersion)}" );
				}
			</c:if>	
		})

		<%-- TODO: the code is duplicated in login_form.jsp --%>
		$$.shell.login = function ()
		{
			$("#errorMessage").text("");
			jQuery.ajax(
			{
				url: "/login.do",
				type: "POST",
				async: true,
				dataType: "json",
				data:
				{
					j_username: $( '[name="j_username"]' ).val(),
					j_password: $( '[name="j_password"]' ).val(),
					responseType: "json"
				},
				success: function( data, textStatus, jqXHR )
				{
					window.location.reload();
				},
				error: function( jqXHR, textStatus, errorThrown ) 
				{
					if( jqXHR.status == 401 )
					{
						$( "#errorMessage" ).text( jqXHR.responseText );
					}
					else
					{
						alert("${l.l('При попытке аутентификации произошла ошибка:')} " + textStatus );
					}
				}	
			});
		}
	</script>

	<%-- TODO: the code is duplicated in login_form.jsp --%>
	<div style="position: absolute; top: 50%; margin-top: -100px;/* half of #content height*/ left: 0; width: 100%;">
		<form class="in-mt05" onsubmit="$$.shell.login(); return false;" 
			style="width: 250px; margin-left: auto; margin-right: auto; height: 200px;  -moz-box-sizing: sborder-box; -webkit-box-sizing: border-box; box-sizing: border-box;">
			<div id="errorMessage" style="color: #FF0000;"></div>
			<input name="j_username" type="text" placeholder="${l.l('Логин')}" style="width: 100%;"/>
			<input name="j_password" type="password" placeholder="${l.l('Пароль')}" style="width: 100%;"/>
			<button type="submit" class="mt05 btn-grey" style="width: 100%;">${l.l('Войти')}</button>
		</form>
	</div>	
</body>
</html>
