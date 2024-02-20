<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<form action="/user/plugin/bgbilling/proto/contract.do" id="${uiid}">
	<input type="hidden" name="action" value="updateGroups"/>
	<input type="hidden" name="billingId" value="${form.param.billingId}"/>
	<input type="hidden" name="contractId" value="${form.param.contractId}"/>

	<ui:select-mult list="${frd.groupList}" values="${frd.selectedGroupIds}" hiddenName="groupId" style="width: 300px;" showId="1"/>

	<div class="mt1">
		<button type="button" class="btn-grey" onclick="$$.ajax.post(this.form).done(() => $$.ajax.load('${form.requestUrl}',$('#${uiid}').parent()))">OK</button>
		<button type="button" class="btn-grey ml1" onclick="$$.ajax.load('${form.requestUrl}',$('#${uiid}').parent());">Oтмена</button>
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
