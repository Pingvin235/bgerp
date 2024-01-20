<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mb1" style="width: 20em;">
	<b>${l.l('Очереди процессов')}:</b>
</div>
<div class="mb1">
	${u:toString(frd.queueTitleList)}
</div>
<div>
	<button type="button" class="btn-white w100p"
		onclick="$(this).closest('td').find('.buttons').show(); $(this).closest('.editor').empty();">${l.l('Close')}</button>
</div>
