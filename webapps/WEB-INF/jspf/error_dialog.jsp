<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="errorDialog" title="${l.l('Сообщение об ошибке')}:">
	<div id="errorDialogMessage">
	
	</div>
</div>

<script>
	$(function() {
		$( "#errorDialog" ).dialog(
		{
			resizable: false,
			width: 640,
			height: 240,
			modal: true,
			autoOpen: false
		});
	});
</script>