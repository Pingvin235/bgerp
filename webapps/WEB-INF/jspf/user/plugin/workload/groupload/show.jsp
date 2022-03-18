<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="data" value="${form.response.data}" />
<c:set var="root" value="${form.response.data.root}" />

<c:set var="uiid" value="${u:uiid()}" />

<html:form action="/user/plugin/workload/groupload.do">
	<input type="hidden" name="action" value="show" />

	<%-- текущий процесс --%>
	<html:hidden property="processId"/>
	<html:hidden property="processTypeId"/>

	<%-- фильтры --%>
	<ui:date-time paramName="date" value="${data.date}"/>
	&#160;<%--
	<ui:combo-single list="${ctxProcessQueueList}" value="${queueId}"
		prefixText="Очередь:" widthTextValue="150px" showFilter="true"
		hiddenName="queueId" />
	&#160; --%>
	<ui:combo-check map="${ctxProcessTypeMap}" values="${data.processTypeIds}"
		prefixText="Типы:" widthTextValue="150px" showFilter="true" available="${data.configProcessTypeIds}"
		paramName="processTypeIds" />
	&#160;
	<ui:combo-check list="${ctxUserGroupList}" values="${data.userGroupIds}"
		prefixText="Группы:" widthTextValue="120px" showFilter="true" available="${data.configUserGroupIds}"
		paramName="userGroupIds" />
	&#160;
	<ui:combo-single value="${data.sort}" hiddenName="sort" widthTextValue="50px">
		<jsp:attribute name="valuesHtml">
			<li value="userGroup">Гр.</li>
			<li value="time">Вр.</li>
			<li value="processType">Проц.</li>
		</jsp:attribute>
	</ui:combo-single>
	&#160;
	<button class="btn-grey" type="button"
		onclick="openUrlToParent( formUrl(this.form), $('#${uiid}') )">OK</button>
</html:form>

<br>

<table id="${uiid}" class="data" style="width: 100%;">

	<tr class="head">
		<td>Время</td>
		<td>Адрес</td>
		<td>Группа</td>
		<td>Тип процесса</td>
	</tr>
	<c:forEach var="row" items="${data.processList}">

		<c:set var="process" value="${row[0]}" />
		<c:set var="priority" value="${process.priority}"/>
		<%@ include file="/WEB-INF/jspf/process_color.jsp"%>
		<c:set var="bgcolor" value="bgcolor='${color}'"/>
		<tr ${bgcolor}>
			<td><nobr>${row[2]!=null?( tu.format( row[1], 'HH:mm' ).concat("-").concat(tu.format( row[2], 'HH:mm' )) ):( tu.format( row[1], 'HH:mm' ) )}</nobr></td>
			<td>${row[3].value}</td>
			<td>${u:objectTitleList(ctxUserGroupList, process.groupIds)}</td>
			<td>${ctxProcessTypeMap[process.typeId]}</td>
		</tr>
	</c:forEach>

</table>

<script>
	$(function() {
		//bgerp.blow.initTable($('#${uiid}'));
	})
</script>