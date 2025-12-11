<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<h1>${l.l('Choose a process for merging the current to')}</h1>
<html:form action="/user/process">
	<html:hidden property="id"/>
	<input type="hidden" name="method" value="processMerge"/>

	<c:set var="mergeProcessUiid" value="${u:uiid()}"/>
	<ui:combo-single name="processId" id="${mergeProcessUiid}" style="width: 100%;"/>

	<script>
		$(() => {
			const processList = openedObjectList({'typesInclude' : ['process']});
			let html = '';
			$.each(processList, function () {
				html += '<li value=\'' + this.id + '\'>' + this.title + '</li>';
			});

			$('#${mergeProcessUiid} ul.drop').html(html);
			$$.ui.comboSingleInit($('#${mergeProcessUiid}'));
		})
	</script>

	<table style="width: 100%;">
		<tr>
			<td valign="top" class="pt1 pb1">
				<c:set var="okCommand">$$.ajax.post(this).done(() => {
					$$.process.remove(${process.id});
					$$.process.open(this.form.processId.value);
				})</c:set>
				<c:set var="cancelCommand">$$.ajax.load('${form.returnUrl}', $('#${form.returnChildUiid}').parent());</c:set>

				<ui:button type="ok" onclick="${okCommand}"/>
				<ui:button type="cancel" styleClass="ml1" onclick="${cancelCommand}"/>
			</td>
		</tr>
	</table>
</html:form>