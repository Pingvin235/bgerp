<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="plugin" value="${ctxPluginManager.pluginMap['bgbilling']}"/>

<c:forEach var="billing" items="${plugin.dbInfoManager.dbInfoList}">
	<c:set var="id" value="bgbilling-${billing.id}"/>

	<c:set var="linkObjectItems" scope="request">
		${linkObjectItems}
		<li value="${id}">Договор ${billing.title}</li>
	</c:set>
	<c:set var="linkObjectForms" scope="request"> 
		${linkObjectForms}
		<form action="/user/plugin/bgbilling/contract.do" id="${id}" style="display: none;">
			<input type="checkbox" name="check" style="display: none;" checked="true"/>
			<input type="hidden" name="processId" value="${form.id}"/>
			<input type="hidden" name="billingId" value="${billing.id}"/>			
			<input type="hidden" name="action" value="addProcessContractLink"/>
			<input style="width: 100%;" name="contractTitle" placeholder="Номер договора"/>
		</form>
	</c:set>
</c:forEach>