<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${fn:startsWith( item.linkedObjectType, 'contract:' )}">
		<c:set var="billingId" value="${fn:substringAfter( item.linkedObjectType, ':')}" scope="request"/>
		<td>${l.l('Договор')}:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>

		<c:set var="contractId" value="${item.linkedObjectId}" scope="request"/>
		<td><a href="#" onclick="bgbilling_openContract( '${billingId}', ${contractId} ); return false;">${item.linkedObjectTitle} [ ${item.linkedObjectComment} ]</a></td>
		
		<c:set var="uiid" value="${u:uiid()}"/>
		<script id="${uiid}">
			$(function()
			{
				$('#${uiid}').closest('tr').find( 'input[type=checkbox]' ).change( function()
				{
					if( this.checked )
					{
					 	var url = "/user/plugin/bgbilling/contract.do?billingId=${billingId}&id=${contractId}&inBuffer=0";
						$('#${searchTabsUiid}').tabs().tabs( "add", url, "${item.linkedObjectTitle}", " id=${billingId}-${contractId}" );
						
						//window.scrollTo( 0, document.body.scrollHeight );
					}
					else
					{
						//$( $customerContractList.find( ">div.ui-tabs-panel" )[pos] ).find( ".ui-tabs" ).tabs( "showTab", billingId + "-" + contractId );
					}
				})
			})			
		</script>
	</c:when>
</c:choose>	
