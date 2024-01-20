<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mb1" style="width: 20em;">
	<b>${l.l('Типы процессов')}:</b>
</div>
<div class="mb1">
	${u:toString( frd.containProcess)}
</div>
<button type="button" class="btn-white w100p" onclick="$(this).parent().parent().find('button').show(); $(this).parent().empty();">${l.l('Close')}</button>
