<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${item.linkObjectType.startsWith('contract:' )}">
		<tr>
			<td>${delButton}</td>
			<td>${item.linkObjectId}</td>
			<c:set var="billingId" value="${su.substringAfter( item.linkObjectType, ':')}" scope="request"/>
			<td>Договор:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>

			<c:set var="contractId" value="${item.linkObjectId}" scope="request"/>
			<td><a href="#" onclick="$$.bgbilling.contract.open( '${billingId}', ${contractId} ); return false;">${item.linkObjectTitle}</a></td>
		</tr>

		<c:set var="processTypeConfig" value="${ctxProcessTypeMap[u:int( form.param['processTypeId'] )].properties.configMap}"/>
		<c:set var="page" value="${processTypeConfig['bgbilling:linkedContractShowJsp']}"/>
		<c:if test="${not empty page}">
			<tr>
				<td colspan="4" style="padding:0"><jsp:include page="${page}"/></td>
			</tr>
		</c:if>

		<c:if test="${not empty processTypeConfig['bgbilling:processShowLinkContractProcess'] }">
			<script>
				 $(function () {
					var $tabs = $("#content > #process-${process.id} #processTabsDiv");
					<c:url var="url" value="/user/process/link.do">
						<c:param name="method" value="linkedProcessList"/>
						<c:param name="objectType" value="${item.linkObjectType}"/>
						<c:param name="objectTitle" value="${item.linkObjectTitle}"/>
						<c:param name="id" value="${item.linkObjectId}"/>
					</c:url>

					<%-- проверка, чтобы добавляло только один раз вкладку --%>
					if ($tabs.find("li:contains('${u:quotEscape(item.linkObjectTitle)}')").length == 0)
						$tabs.tabs("add", "${url}", "Процессы договора ${u:quotEscape( item.linkObjectTitle )}");
			  	})
			</script>
		</c:if>
	</c:when>
	<c:when test="${item.linkObjectType.startsWith('bgbilling-helpdesk:' )}">
		<tr>
			<td>${delButton}</td>
			<td>${item.linkObjectId}</td>
			<c:set var="billingId" value="${su.substringAfter( item.linkObjectType, ':')}"/>
			<td>HelpDesk:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>
			<td></td>
		</tr>
	</c:when>
</c:choose>
