<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${item.linkedObjectType.startsWith('contract:' )}">
		<c:set var="billingId" value="${su.substringAfter( item.linkedObjectType, ':')}" scope="request"/>
		<td>Договор:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>

		<c:set var="contractId" value="${item.linkedObjectId}" scope="request"/>
		<td><a href="#" onclick="$$.bgbilling.contract.open('${billingId}', ${contractId}); return false;">${item.linkedObjectTitle} [ ${item.linkedObjectComment} ]</a></td>

		<c:set var="uiid" value="${u:uiid()}"/>
		<script id="${uiid}">
			$$.bgbilling.contract.onCheckTabOpen('${searchTabsUiid}', '${uiid}', '${item.linkedObjectTitle}', '${billingId}', '${contractId}');
		</script>
	</c:when>
</c:choose>
