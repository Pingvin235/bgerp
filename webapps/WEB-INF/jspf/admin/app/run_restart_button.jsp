<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<ui:button styleClass="ml1" type="run" onclick="
	$$.ajax.post(this, {failAlert: false})
		.fail(() => {alert('${l.l('Выполнен перезапуск, обновите страницу браузера.')}')});"/>
