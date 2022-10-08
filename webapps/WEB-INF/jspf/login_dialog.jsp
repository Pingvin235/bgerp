<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<iframe name="loginFormTarget" style="display:none"></iframe>

<form id="loginDialog" method="post" style="display:none;" target="loginFormTarget" class="in-mt05"
	style="width: 250px; margin-left: auto; margin-right: auto; height: 200px; box-sizing: border-box;">
	<%@ include file="login_form_inputs.jsp"%>
</form>

<script>
	$(function () {
		const $dialog = $$.shell.$loginDialog();
		$dialog.dialog({
			modal: true,
			draggable: false,
			resizable: false,
			title: "${l.l('Требуется повторная авторизация')}",
			position: { my: "center top", at: "center top+100px", of: window }
		});
		$dialog.dialog("close");

		$dialog.submit(() => {
			$$.shell.login().done(() => {
				$("#loginErrorMessage").text("");
				$dialog.dialog("close");
				$$.timer.start();
			})
		});
	});
</script>
