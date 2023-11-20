<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<form action="${form.httpRequestURI}" id="${uiid}" class="in-mr05 in-mb05-all">
	<input type="hidden" name="action" value="search"/>

	<ui:date-time type="ymdhms" paramName="timeFrom" value="${empty form.param.timeFrom ? 'first' : form.param.timeFrom}" placeholder="From time"/>
	<ui:date-time type="ymdhms" paramName="timeTo" value="${form.param.timeTo}" placeholder="To time"/>

	<input type="text" name="ipAddress" value="${form.param.ipAddress}" size="15" placeholder="IP address"/>
	<input type="text" name="parameter" value="${form.param.parameter}" size="15" placeholder="Parameters substring"/>

	<c:set var="groupListId" value="${u:uiid()}"/>
	<c:set var="executorListId" value="${u:uiid()}"/>
	<ui:combo-check
		id="${groupListId}" list="${ctxUserGroupList}" paramName="groupId" values="${form.getParamValues('groupId')}"
		prefixText="${l.l('Groups')}:" widthTextValue="10em" onChange="updateExecutors($('#${groupListId}'), $('#${executorListId}'), 'groupId', 'userId');"/>

	<ui:combo-check
		id="${executorListId}" list="${ctxUserList}" paramName="userId" values="${form.getParamValues('userId')}"
		prefixText="${l.l('Users')}:" widthTextValue="10em" styleClass="mr05"/>

	<ui:combo-perm-tree-check permTrees="${permTrees}" values="${form.getParamValuesStr('perm')}"
		prefixText="${l.l('Actions')}:" widthTextValue="15em"/>

	<ui:button type="out" onclick="$$.ajax.load(this.form, $('#${uiid}').parent())" styleClass="ml05"/>

	<ui:page-control nextCommand="; $$.ajax.load(this.form, $('#${uiid}').parent())"/>
</form>

<table class="data hl mt1">
	<tr>
		<td>ID</td>
		<td>Time</td>
		<td nowrap="1">IP address</td>
		<td>User</td>
		<td>Action</td>
		<td>Parameters</td>
		<td>Duration (ms)</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td nowrap="nowrap" align="center">${item.id}</td>
			<td nowrap="nowrap" align="center">${tu.format(item.time, 'ymdhms')}</td>
			<td nowrap="nowrap" align="center">${item.ipAddress}</td>
			<td>${ctxUserMap[item.userId].title}</td>
			<td>${item.action}</td>
			<td>
				<c:set var="maxLength" value="150"/>
				<c:set var="text" value="${item.parameters}"/>
				<%@include file="/WEB-INF/jspf/short_text.jsp"%>
			</td>
			<td>${item.duration}</td>
		</tr>
	</c:forEach>
</table>

<shell:title text="Action Log"/>
