<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Выберите процесс для слияния текущего')}</h1>
<html:form action="/user/process">
	<html:hidden property="id"/>
	<input type="hidden" name="action" value="processMerge"/>
	
	<c:set var="mergeProcessUiid" value="${u:uiid()}"/>
	<ui:combo-single hiddenName="processId" id="${mergeProcessUiid}" style="width: 100%;"/>
	
	<script>
		$(() => {
			const processList = openedObjectList({'typesInclude' : ['process']});
			let html = '';
			$.each(processList, function() {
				html += '<li value=\'' + this.id + '\'>' + this.title + '</li>';
			});

			$('#${mergeProcessUiid} ul.drop').html(html);
			$$.ui.comboSingleInit($('#${mergeProcessUiid}'));
		})
	</script>
	
	<table style="width: 100%;">
		<tr>
			<td valign="top" class="pt1 pb1">
				<c:set var="saveCommand">$$.ajax.post(this.form).done(() => {
					$$.closeObject = null;
					$$.shell.removeCommandDiv('process-${process.id}');
					$$.process.open(this.form.processId.value);
				})</c:set>
				<c:set var="closeEditor">$$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent());</c:set>	
			
				<button class="btn-grey mr1" type="button" onclick="${saveCommand}">OK</button>
				<button class="btn-grey mr1" type="button" onclick="${closeEditor}">${l.l('Отмена')}</button>
			</td>
		</tr>
	</table>
</html:form>