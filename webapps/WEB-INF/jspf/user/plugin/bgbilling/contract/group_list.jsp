<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<form action="plugin/bgbilling/proto/contract.do" id="${uiid}">
	<input type="hidden" name="action" value="updateGroups"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>
	
	<u:sc>
		<c:set var="list" value="${form.response.data.groupList}"/>
		<c:set var="values" value="${form.response.data.selectedGroupIds}"/>
		<c:set var="hiddenName" value="groupId"/>
		<c:set var="style" value="width: 300px;"/>
		<c:set var="showId" value="1"/>
		<%@ include file="/WEB-INF/jspf/select_mult.jsp"%>
	</u:sc>
	
	<div class="mt1">
		<button type="button" class="btn-grey" onclick="if(sendAJAXCommand(formUrl(this.form))) { openUrlToParent('${form.requestUrl}',$('#${uiid}')); }">OK</button>
		<button type="button" class="btn-grey ml1" onclick="openUrlToParent('${form.requestUrl}',$('#${uiid}'));">Oтмена</button>
	</div>
</form>	


<c:set var="contractTreeId" value="bgbilling-${form.param.billingId}-${form.param.contractId}-tree"/>
<script>
	$(function()
	{
		var groups = "";
		
		var $groups = $('#${uiid} .drop-list span.title');
		$groups.each( function( i )
		{
			var text = $(this).text();
			
			groups += "<div>" + $.trim( text.substring( 0, text.lastIndexOf( '(' ) ) );
			if( index !== ($groups.length - 1) )
			{
				groups += ", ";
			}
			groups += "</div>";
		});
			
		$('#${contractTreeId} #treeTable div#groups').html( groups );	
	})
</script>
