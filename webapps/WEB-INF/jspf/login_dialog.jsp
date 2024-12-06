<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<form id="loginDialog" method="post" style="display: none;" class="in-mt05"
	style="width: 250px; margin-left: auto; margin-right: auto; height: 200px; box-sizing: border-box;">
	<%@ include file="login_form_inputs.jsp"%>
</form>

<script>
	$(function () {
		$$.shell.login.init({
			modal: true,
			draggable: false,
			resizable: false,
			title: "${l.l('Re-login is required')}",
			position: { my: "center top", at: "center top+100px", of: window }
		});
	})
</script>
