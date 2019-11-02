<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<tr>
	<td valign="top" ${tdAttrs} class="pt1 pb1">
		<c:set var="closeEditor">openUrlToParent( '${form.returnUrl}', $('#${form.returnChildUiid}') );</c:set>	
		<c:set var="saveCommand">if( sendAJAXCommand( formUrl( this.form ) ) ){ ${closeEditor} }</c:set>
	
		<button class="btn-grey mr1" type="button" onclick="${saveCommand}">ОК</button>
		<button class="btn-grey mr1" type="button" onclick="${closeEditor}">Отмена</button>
	</td>	
</tr>
<c:set var="colspan" value=""/>	