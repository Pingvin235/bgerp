<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div class="mb1">
	<b>${l.l('Типы процессов')}:</b>
</div>
<div class="mb1">
	${u:toString( form.response.data.containProcess)}
</div>
<button type="button" style="width:100%" class="btn-grey" onclick="$(this).parent().parent().find('button').show(); $(this).parent().empty();">${l.l('Отмена')}</button>  



