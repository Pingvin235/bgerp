<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="title">
	<c:choose>
		<c:when test="${not empty filter.title}">${filter.title}</c:when>
		<c:otherwise>Группы решения</c:otherwise>
	</c:choose>
</c:set>

<%-- TODO: Use tag as in webapps\WEB-INF\jspf\admin\plugin\log\action.jsp --%>
<c:set var="code">
	<u:sc>
		<c:set var="id" value="${groupListId}"/>
		<c:set var="paramName" value="${groupParamName}"/>
		<c:set var="list" value="${ctxUserGroupList}"/>
		<c:set var="values" value="${filter.defaultValues}"/>

		<c:if test="${not empty savedParamsFilters.getSelectedValues(groupParamName)}">
			<c:set var="values" value="${savedParamsFilters.getSelectedValues(groupParamName)}"/>
		</c:if>

		<c:set var="savedExecutors" value="${u:toString( savedParamsFilters.getSelectedValuesListStr(executorParamName) )}"/>
		<c:set var="available" value="${filter.availableValues}"/>
		<c:set var="showFilter" value="1"/>
		<c:set var="prefixText" value="${empty filter.title ? 'Группы:' : filter.title.concat(':')}"/>
		<c:set var="widthTextValue" value="150px"/>
		<c:set var="onChange">updateExecutors($('#${groupListId}'), $('#${executorListId}'), '${groupParamName}', '${executorParamName}' , '${savedExecutors}');</c:set>
		<%@ include file="/WEB-INF/jspf/combo_check.jsp"%>
	</u:sc>
</c:set>

<%@ include file="item.jsp"%>
