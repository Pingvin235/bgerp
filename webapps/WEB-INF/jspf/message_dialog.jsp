<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="messageDialog" title="${l.l('Сообщение об ошибке')}">
	<div id="messageDialogMessage">

	</div>
</div>

<script>
	$(function () {
		$$.shell.message.init({
			resizable: false,
			width: 640,
			height: 240,
			modal: true,
			autoOpen: false,
			position: { my: "center top", at: "center top+100px", of: window },
			buttons: [{
				text: "OK",
				class: "btn-white m05",
				click: () => $$.shell.message.close()
			}]
		});
	})
</script>