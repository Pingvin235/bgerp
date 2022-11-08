<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="closeScript">$(this).closest('td').find('.buttons').show(); $(this).closest('.editor').empty();</c:set>

<form action="/admin/process.do">
	<input type="hidden" name="action" value="typeCopy"/>
	<input type="hidden" name="id" value="${form.param.id}"/>
	<div class="mb1">
		<b>${l.l('Копировать свойства из')}:</b>
	</div>
	<div class="mb1">
		<ui:select-single
			hiddenName="fromId" list="${form.response.data.types}" style="width: 300px;"
			onSelect="$(this).closest('form').find('button.ok').show()"
		/>
	</div>
	<div>
		<button type="button" class="btn-grey mr1 ok" style="display: none;" onclick="$$.ajax.post(this.form).done(() => { ${closeScript} })">OK</button>
		<button type="button" class="btn-white" onclick="${closeScript}">${l.l('Close')}</button>
	</div>
</form>
