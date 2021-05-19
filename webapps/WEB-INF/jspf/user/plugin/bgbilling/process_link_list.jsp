<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:choose>
	<c:when test="${fn:startsWith( item.linkedObjectType, 'contract:' )}">
		<tr>
			<td>${delButton}</td>
			<td>${item.linkedObjectId}</td>
			<c:set var="billingId" value="${fn:substringAfter( item.linkedObjectType, ':')}" scope="request"/>
			<td>${l.l('Договор')}:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>

			<c:set var="contractId" value="${item.linkedObjectId}" scope="request"/>
			<td><a href="#" onclick="bgbilling_openContract( '${billingId}', ${contractId} ); return false;">${item.linkedObjectTitle}</a></td>
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
						<c:param name="action" value="linkedProcessList"/>
						<c:param name="objectType" value="${item.linkedObjectType}"/>
						<c:param name="objectTitle" value="${item.linkedObjectTitle}"/>
						<c:param name="id" value="${item.linkedObjectId}"/>
					</c:url>
					
					<%-- проверка, чтобы добавляло только один раз вкладку --%>					
					if ($tabs.find("li:contains('${u:quotEscape(item.linkedObjectTitle)}')").length == 0)
						$tabs.tabs("add", "${url}", "Процессы договора ${u:quotEscape( item.linkedObjectTitle )}");
			  	})
			</script>
		</c:if>
	</c:when>
	<%-- <c:when test="${item.linkedObjectType eq 'bgbilling-commonContract'}">
		<tr>
			<td><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td>${item.linkedObjectId}</td>
			<td nowrap="nowrap">Единый договор</td>
			<td><a href="#" onclick="bgbilling_openCommonContract( ${item.linkedObjectId} ); return false;">${item.linkedObjectTitle}</a></td>
		</tr>
	</c:when>
	<c:when test="${fn:startsWith( item.linkedObjectType, 'bgbilling-task:' )}">
		<tr>
			<td><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
			<td>${item.linkedObjectId}</td>
			<c:set var="billingId" value="${fn:substringAfter( item.linkedObjectType, ':')}"/>
			<td>Задача:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>
			<td>${item.linkedObjectTitle}</td>
		</tr>
	</c:when> --%>
	<c:when test="${fn:startsWith( item.linkedObjectType, 'bgbilling-helpdesk:' )}">
		<tr>
			<td>${delButton}</td>
			<td>${item.linkedObjectId}</td>
			<c:set var="billingId" value="${fn:substringAfter( item.linkedObjectType, ':')}"/>
			<td>HelpDesk:${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>
			<td></td>
		</tr>
	</c:when>
</c:choose>	
