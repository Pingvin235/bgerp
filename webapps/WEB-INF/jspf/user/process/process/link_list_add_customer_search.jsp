<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<p:check action="ru.bgcrm.struts.action.SearchAction:customerSearch">
	<c:set var="id" value="${u:uiid()}"/>
	
	<c:set var="linkObjectItems" scope="request">
		${linkObjectItems}
		<li value="${id}">${l.l('Контрагент')}</li>
	</c:set>
	<c:set var="linkObjectForms" scope="request">
		<c:set var="resultUiid" value="${u:uiid()}"/>

		${linkObjectForms}
		<form action="/user/search.do" id="${id}" style="display: none;" onsubmit="$$.ajax.load(this, $('#${resultUiid}')); return false;">
			<input type="hidden" name="action" value="customerSearch"/>
			<input type="hidden" name="searchBy" value="title"/>
			<input type="hidden" name="forwardFile" value="/WEB-INF/jspf/user/process/process/link_list_search_customer.jsp"/>
			<input type="hidden" name="processId" value="${form.id}"/>
			
			<ui:input-text style="width: 100%;" name="title" placeholder="${l.l('Строка поиска контрагента')}"
				onSelect="$$.ajax.load($(this).closest('form'), $('#${resultUiid}'))"/>

			<div id="${resultUiid}"></div>
		</form>
	</c:set>
</p:check>