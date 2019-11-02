<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="data" value="${data.stepData}"/>
<c:set var="resultId" value="${u:uiid()}"/>

<div>
	<table>
	<tr>
		<td>Номер договора:</td>
		<td>
			<form action="/user/plugin/bgbilling/search.do">
			<input type="hidden" name="action" value="contractSearch"/>
			<input type="hidden" name="searchBy" value="title"/>
			<input type="hidden" name="forwardFile" value="/WEB-INF/jspf/usermob/plugin/bgbilling/step_find_contract_search_result.jsp"/>
			<input type="hidden" name="billing" value="${stepData.billingId}"/>
			<input name="title" type="text" size="20"/>
		</td>
		<td>
			<input type="button" value="Найти" onclick="openUrlTo( formUrl( this.form ), $('#${resultId}') )">
			</form>
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
			<form action="/user/link.do">
			<input type="hidden" name="action" value="addLink"/>
			<input type="hidden" name="objectType" value="process"/>
			<input type="hidden" name="objectId" value="${process.id}"/>
			<input type="hidden" name="linkedObjectType" value="contract:${stepData.billingId}"/>
			<input type="hidden" name="linkedObjectTitle" id="linkedObjectTitle"/>
			<select id="${resultId}" style="width:100%;" onchange=" $('#linkedObjectTitle').val( $('#${resultId} option:selected').text() );"></select>
		</td>
		<td>
			<input type="button" value="Выбрать" onclick="sendAJAXCommand( this.form ); ${reopenProcessEditorCode}"/>
			</form>
		</td>
	</tr>
	</table>
</div>