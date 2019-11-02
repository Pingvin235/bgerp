<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<u:sc>
	<c:set var="id" value="groups"/>
	
	<c:set var="valuesHtml">
		<li value="-1">не выбрана группа</li>
	</c:set>
	<c:set var="list" value="${form.response.data.groupList}"/>
	<c:set var="hiddenName" value="groupId"/>
	<c:set var="prefixText" value="Группы:"/>
	<c:set var="onSelect">
		listGroupQuarters();
	</c:set>
	<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>	
</u:sc>