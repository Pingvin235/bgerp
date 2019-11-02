<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<iframe name="loginFormTarget" style="display:none"></iframe>

<form id="loginForm" method="post" style="display:none;" target="loginFormTarget" class="in-mt05" 
	style="width: 250px; margin-left: auto; margin-right: auto; height: 200px;  -moz-box-sizing: sborder-box; -webkit-box-sizing: border-box; box-sizing: border-box;">
	<div id="errorMessage" style="color: #FF0000;"></div>
	<input name="j_username" type="text" placeholder="${l.l('Логин')}" style="width: 100%;"/>
	<input name="j_password" type="password" placeholder="${l.l('Пароль')}" style="width: 100%;"/>
	<button type="submit" class="mt05 btn-grey" style="width: 100%;">${l.l('Войти')}</button>
</form>

<script>
	$(function() 
	{
		$('#loginForm').dialog({
			modal: true,
			draggable: false,
			resizable: false,
		    title: "${l.l('Требуется повторная авторизация')}",
		    position: { my: "center top", at: "center top+100px", of: window }
		});
		$('#loginForm').dialog( "close" );
		
		$('#loginForm').submit( function() 
		{
			<%-- TODO: the code is duplicated in login.jsp --%>
			jQuery.ajax(
			{
				url: "/login.do",
				type: "POST",
				async: false,
				dataType: "json",
				data:
				{
					j_username: $( '[name="j_username"]' ).val(),
					j_password: $( '[name="j_password"]' ).val(),
					responseType: "json"
				},
				success: function( data, textStatus, jqXHR )
				{
					$( "#errorMessage" ).text("");
					$('#loginForm').dialog( "close" );
				},
				error: function( jqXHR, textStatus, errorThrown ) 
				{
					if( jqXHR.status == 401 )
					{
						$( "#errorMessage" ).text( jqXHR.responseText );
					}
					else
					{
						alert( "${l.l('При попытке аутентификации произошла ошибка:')} " + textStatus );
					}
				}	
			});
		});
	});
</script>