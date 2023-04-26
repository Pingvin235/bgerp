<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<c:set var="inputUiid" value="${u:uiid()}"/>

<div id="${uiid}">
	<c:set var="editCommand">
		$('#${uiid} #selected').hide();
		$('#${uiid} #select').show();
		$('#${uiid} *.searchResult').hide();
	</c:set>

	<c:choose>
		<c:when test="${not empty stepData.customer}">
			<c:set var="displaySelect">style="display: none;"</c:set>
		</c:when>
		<c:otherwise>
			<c:set var="displaySelected">style="display: none;"</c:set>
		</c:otherwise>
	</c:choose>

	<div id="selected" class="tableIndent" ${displaySelected}>
		Контрагент: <a href="#" onclick="${editCommand}">${stepData.customer.title}</a>
	</div>

	<c:set var="sendFormCommand">if( openUrlTo( formUrl( this.form ), $('#${uiid} #searchResult') ) ){ $('#${uiid} *.searchResult').show(); };</c:set>

	<div id="select" class="tableIndent" ${displaySelect}>
		<html:form action="/user/search" onsubmit="return false;">
			<input type="hidden" name="action" value="customerSearch"/>
			<input type="hidden" name="searchBy" value="title"/>
			<input type="hidden" name="processId" value="${process.id}"/>
			<input type="hidden" name="forwardFile" value="/WEB-INF/jspf/usermob/process/process/wizard/step_link_customer_search_result.jsp"/>
			<input type="hidden" name="returnChildUiid" value="${uiid}"/>

			<div style="display: table-cell; width: 100%;">
				<input type="text" name="title"
						id="customerTitle"
						style="width: 100%;" id="${inputUiid}"
						value="${stepData.customer.title}"
						onkeypress="if( enterPressed( event ) ){ ${sendFormCommand} }"/>
			</div>

			<div style="display: table-cell; white-space: nowrap;">
				<button type="button" class="btn-white ml05" onclick="${sendFormCommand}">&gt;&gt;&gt;</button>
			</div>
		</html:form>
	</div>

	<div id="searchResult" class="searchResult" style="display: none;">
		<%-- сюда выводятся результаты поиска --%>
	</div>

	<c:set var="createAndLinkCode">
		var result = sendAJAXCommand( '/user/customer.do?action=customerCreate' );
		if( result )
		{
			var customerId = result.data.customer.id;
			var customerTitle = $('#${uiid} input#customerTitle')[0].value;

			const url = '/user/customer.do?action=customerUpdate&' + $$.ajax.requestParamsToUrl({ 'id': customerId, 'title': customerTitle, parameterGroupId: ${stepData.paramGroupId} });
			$$.ajax.post(url).done(() => {
				if( deleteLinksWithType( 'process', ${process.id}, 'customer' ) &&
					addLink( 'process', ${process.id}, 'customer', customerId, customerTitle ) )
				{
					${reopenProcessEditorCode}
				}
			});
		}
	</c:set>

	<table class="searchResult mt05" style="width: 100%; display: none;">
		<tr>
			<c:choose>
				<c:when test="${not empty stepData.customer}">
					<td width="50%"><button type="button" class="btn-grey" onclick="$('#${uiid} #select').hide(); $('#${uiid} #selected').show(); $('#${uiid} *.searchResult').hide ();" style="width: 100%">${l.l('Отмена')}</button></td>
					<td width="50%"><button type="button" class="btn-grey ml1" onclick="${createAndLinkCode}" style="width: 100%">Создать контрагента</button></td>
				</c:when>
				<c:otherwise>
					<td width="100%"><button type="button" class="btn-grey" onclick="${createAndLinkCode}" style="width: 100%">Создать контрагента</button></td>
				</c:otherwise>
			</c:choose>
		</tr>
	</table>
</div>
