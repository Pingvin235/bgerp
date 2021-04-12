<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="contract" value="${form.response.data.contract}"/>

<c:if test="${not empty contract}">
	<c:set var="uiid" value="${u:uiid()}"/>

	<div class="in-table-cell nowrap in-pr1 mb05" id="${uiid}">
		<div style="width: 100%;">ID: <b>${contract.id}</b></div>
		
		<div>
			Период: <b>${u:formatDate( contract.dateFrom, 'ymd' )} - ${u:formatDate( contract.dateTo, 'ymd' )}</b>
		</div>		
		<div>
			<c:url var="url" value="plugin/bgbilling/commonContract.do">
				<c:param name="action" value="copyParamToContract"/>
				<c:param name="customerId" value="${customer.id}"/>
				<c:param name="commonContractId" value="${contract.id}"/>
			</c:url>
			<button type="button" class="btn-white btn-small" onclick="if( confirm( 'Скопировать параметры?' ) ) { sendAJAXCommand( '${url}' ) }">Скопировать параметры</button>
			
			<c:url var="url" value="plugin/bgbilling/commonContract.do">
				<c:param name="id" value="${contract.id}"/>
				<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/common_contract/common_contract_edit.jsp"/>
			</c:url>
			<c:set var="editCommand">openUrlToParent('${url}', $('#${uiid}') )</c:set>
			
			<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
		</div>			
	</div>	
	
	<%-- 
	<table style="width: 100%;">
		<tr>
			<td class="title" nowrap="nowrap">ID: <b>${contract.id}</b></td>
			<td colspan="3" style="text-align:right;">
				<c:url var="url" value="plugin/bgbilling/commonContract.do">
					<c:param name="id" value="${contract.id}"/>
					<c:param name="forwardFile" value="/WEB-INF/jspf/user/plugin/bgbilling/common_contract/common_contract_edit.jsp"/>
				</c:url>
				[<a href="#" onclick="openTabUrlPos('${url}', -1)">Редактировать</a>]
			</td>
		</tr>
		<tr>				
			<td class="box" nowrap="nowrap">
				Контрагент: <a href="#" onclick="openCustomer( ${customer.id} ); return false;">${customer.title}</a>
			</td>
			<td class="box" nowrap="nowrap" style="width: 100%;">
				Адрес: ${contract.address.value}
			</td>
			<td class="box" nowrap="nowrap">
				Период: <fmt:formatDate value="${contract.dateFrom}" pattern="dd.MM.yyyy"/> - <fmt:formatDate value="${contract.dateTo}" pattern="dd.MM.yyyy"/>
			</td>
			<td class="box" nowrap="nowrap" align="right">
				<c:url var="url" value="plugin/bgbilling/commonContract.do">
					<c:param name="action" value="copyParamToContract"/>
					<c:param name="customerId" value="${customer.id}"/>
					<c:param name="commonContractId" value="${contract.id}"/>
				</c:url>
				<input type="button" onclick="if( confirm( 'Скопировать параметры?' ) ) { sendAJAXCommand( '${url}' ) }" value="Скопировать параметры"/>
			</td>
		</tr>
	</table>
	--%>
	
	<script>
		$(function()
		{
			//$("a[href='#bgbilling-commonContractTabs-${contract.id}']").text( '${contract.formatedNumber}' );
			
			var $tabs = $("#bgbilling-commonContract-${contract.id}-Tabs").tabs({refreshButton: true});
			
			<c:url var="url" value="parameter.do">
				<c:param name="action" value="parameterList"/>
				<c:param name="id" value="${contract.id}"/>
				<c:param name="objectType" value="bgbilling-commonContract"/>
				<c:param name="parameterGroup" value="-1"/>
			</c:url>
			$tabs.tabs( 'add', "${url}", "Параметры" );
			
			<c:url var="url" value="/user/process/link.do">
		   		<c:param name="action" value="linkedProcessList"/>
		   		<c:param name="objectType" value="bgbilling-commonContract"/>
		   		<c:param name="objectTitle" value="${contract.formatedNumber}"/>
				<c:param name="id" value="${contract.id}"/>
			</c:url>
			
		   	$tabs.tabs( "add", "${url}", "Процессы" );

			<c:set var="plugin" value="${ctxPluginManager.pluginMap['document']}"/>
			<c:if test="${not empty plugin}">
				<c:url var="url" value="plugin/document/document.do">
					<c:param name="scope" value="bgbilling-commonContract"/>
					<c:param name="objectType" value="bgbilling-commonContract"/>
					<c:param name="objectTitle" value="${contract.formatedNumber}"/>
					<c:param name="objectId" value="${contract.id}"/>							
				</c:url>
				$tabs.tabs( 'add', "${url}", "Документы" );
			</c:if> 
			
			<%-- договора данного ЕД --%>
			<c:forEach items="${contractList}" var="link" varStatus="status">
				<c:set var="billingId" value="${fn:substringAfter( link.linkedObjectType, ':' )}"/>
			
				<c:set var="liAttrs">id='${billingId}-${link.linkedObjectId}'</c:set>
				<c:if test="${status.first}">
					<c:set var="liAttrs">${liAttrs} style='margin-left: 1em;'</c:set>
				</c:if>
			
				$tabs.tabs( 'add', '/user/plugin/bgbilling/contract.do?billingId=${billingId}&id=${link.linkedObjectId}&inBuffer=0', '${link.linkedObjectTitle}', "${liAttrs}" );
				
				/* $tabs.tabs().tablist.append( "<li ${style} id='${billingId}-${link.linkedObjectId}'><a href='/user/plugin/bgbilling/contract.do?billingId=${billingId}&id=${link.linkedObjectId}'>${link.linkedObjectTitle}</a></li>" );
				$tabs.tabs( "refresh" ); */
			</c:forEach>
			
		})
	</script>
	
	<%--
	<u:sc>
		<c:set var="title">
			<span class='title'>ЕД: ${contract.formatedNumber}</span>
		</c:set>
		<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>
		<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
	</u:sc>
	 --%>
</c:if>

<div id="bgbilling-commonContract-${contract.id}-Tabs">
	<ul></ul>
</div>
