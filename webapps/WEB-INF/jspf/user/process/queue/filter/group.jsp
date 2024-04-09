<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title">
	<c:choose>
		<c:when test="${not empty filter.title}">${filter.title}</c:when>
		<c:otherwise>${l.l('Groups')}</c:otherwise>
	</c:choose>
</c:set>

<%-- TODO: Use tag as in webapps\WEB-INF\jspf\admin\plugin\log\action.jsp --%>
<c:set var="code">
	<u:sc>
		<c:set var="values" value="${filter.defaultValues}"/>

		<c:if test="${not empty savedParamsFilters.getParamValues(groupParamName)}">
			<c:set var="values" value="${savedParamsFilters.getParamValues(groupParamName)}"/>
		</c:if>

		<c:set var="savedExecutors" value="${u.toString( savedParamsFilters.getParamValuesListStr(executorParamName) )}"/>

		<ui:combo-check id="${groupListId}"
			paramName="${groupParamName}"
			list="${ctxUserGroupList}"
			values="${values}"
			available="${filter.availableValues}"
			showFilter="1"
			prefixText="${empty filter.title ? l.l('Groups') : filter.title}:"
			widthTextValue="10em"
			onChange="updateExecutors($('#${groupListId}'), $('#${executorListId}'), '${groupParamName}', '${executorParamName}' , '${savedExecutors}');"/>
	</u:sc>
</c:set>

<%@ include file="item.jsp"%>
