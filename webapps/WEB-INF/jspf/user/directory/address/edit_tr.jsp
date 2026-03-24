<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td colspan="2" class="in-mr1">
		<p:check action="ru.bgcrm.struts.action.DirectoryAddressAction:addressUpdate">
			<button type="button" class="btn-grey" onclick="$$.ajax.post(this).done(() => $$.ajax.loadContent('${form.returnUrl}', this))">OK</button>
		</p:check>
		<button type="button" class="btn-white" onclick="$$.ajax.loadContent('${form.returnUrl}', this)">${l.l('Cancel')}</button>
	</td>
</tr>