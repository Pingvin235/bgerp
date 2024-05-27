<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="ru.bgcrm.plugin.fulltext.action.SearchAction:search">
	<c:set var="config" value="${ctxSetup.getConfig('ru.bgcrm.plugin.fulltext.model.Config')}"/>

	<c:if test="${not empty config and config.objectTypeMap.containsKey('customer')}">
		<c:set var="id" value="${u:uiid()}"/>

		<c:set var="linkObjectItems" scope="request">
			${linkObjectItems}
			<li value="${id}">${l.l('FullText: Контрагент')}</li>
		</c:set>
		<c:set var="linkObjectForms" scope="request">
			<c:set var="resultUiid" value="${u:uiid()}"/>

			${linkObjectForms}
			<form action="/user/plugin/fulltext/search.do" id="${id}" style="display: none;" onsubmit="$$.ajax.load(this, $('#${resultUiid}')); return false;">
				<input type="hidden" name="method" value="search"/>
				<input type="hidden" name="objectType" value="customer"/>
				<input type="hidden" name="forwardFile" value="/WEB-INF/jspf/user/process/process/link_list_search_customer.jsp"/>
				<input type="hidden" name="processId" value="${form.id}"/>

				<ui:input-text style="width: 100%;" name="filter" placeholder="${l.l('Строка поиска контрагента')}"
					onSelect="$$.ajax.load($(this).closest('form'), $('#${resultUiid}'))"/>

				<div id="${resultUiid}"></div>
			</form>
		</c:set>
	</c:if>
</p:check>