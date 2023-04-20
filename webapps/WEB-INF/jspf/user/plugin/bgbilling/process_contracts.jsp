<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<table style="width: 100%;" class="data">
	<tr>
		<td>Биллинг</td>
		<td>ID</td>
		<td>Номер</td>
		<td></td>
	</tr>

	<c:forEach items="${form.response.data.list}" var="link">
		<c:set var="billingId" value="${su.substringAfter( link.linkedObjectType, ':' )}"/>
		<c:set var="customerId" value="${form.response.data.customerId}" />
		<tr>
			<td>${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[billingId].title}</td>
			<td>${link.linkedObjectId}</td>
			<td width="100%"><a href="#" onclick="$$.bgbilling.contract.open( '${billingId}', '${link.linkedObjectId}' ); return false;">${link.linkedObjectTitle}</a></td>
		</tr>
	</c:forEach>
</table>
