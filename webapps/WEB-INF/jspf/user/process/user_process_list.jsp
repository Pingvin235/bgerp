<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="user/process" styleClass="mb05" styleId="${uiid}">
	<input type="hidden" name="action" value="userProcessList"/>

	<div class="tableIndent in-mb05-all">
		Дата создания:
		<input type="text" name="createDate" class="mr1" value="${form.param.createDate}" onchange=""/>
 		<c:set var="selector" value="#${uiid} input[name='createDate']" />
 		<c:set var="editable" value="1" />
		<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

		Дата закрытия:
		<input type="text" name="closeDate" value="${form.param.closeDate}" onchange="" class="mr1"/>
		<c:set var="selector" value="#${uiid} input[name='closeDate']" />
		<c:set var="editable" value="1" />
		<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

		<u:sc>
			<c:set var="valuesHtml" >
				<li value="">Любой</li>
				<c:forEach var="type" items="${form.response.data.typeList}">
					<li value="${type}">${ctxProcessTypeMap[type]}</li>
				</c:forEach>
			</c:set>
		 	<c:set var="styleClass" value="mr1"/>
			<c:set var="hiddenName" value="typeId" />
			<c:set var="value" value="${form.param.typeId}" />
			<c:set var="widthTextValue" value="200px" />
			<c:set var="prefixText" value="Тип:" />
			<c:set var="showFilter" value="1"/>
			<c:set var="onSelect" value="openUrlToParent( formUrl( $('#${uiid}') ), $('#${uiid}') )"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
		</u:sc>

		<u:sc>
			<c:set var="valuesHtml">
				<li value="1">Открытые</li>
				<li value="0">Закрытые</li>
				<li value="">Все</li>
			</c:set>
			<c:set var="styleClass" value="mr1"/>
			<c:set var="hiddenName" value="open" />
			<c:set var="value" value="${form.param.closed}" />
			<c:set var="widthTextValue" value="100px" />
			<c:set var="prefixText" value="Закрыт:" />
			<c:set var="onSelect" value="openUrlToParent( formUrl( $('#${uiid}') ), $('#${uiid}') )"/>
			<%@ include file="/WEB-INF/jspf/combo_single.jsp"%>
		</u:sc>

		<c:set var="nextCommand" value="; openUrlToParent( formUrl( $('#${uiid}') ), $('#${uiid}') )"/>
		<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
	</div>
</html:form>

<c:set var="uiid" value="${u:uiid()}"/>

<%@ include file="/WEB-INF/jspf/table_row_edit_mode.jsp"%>

<c:if test="${not empty form.response.data.list}">
	<table class="data" class="center1020" id="${uiid}" style="width: 100%;">
		<tr>
			<td>ID</td>
			<td>Время создания</td>
			<td>Время закрытия</td>
			<td>Тип</td>
			<td>Статус</td>
			<td>Описание</td>
		</tr>
		<c:forEach var="process" items="${form.response.data.list}">
			<tr openCommand="openProcess(${process.id })">
				<td nowrap="nowrap"><a href="#UNDEF" onclick="openProcess(${process.id}); return false;">${process.id}</a></td>
				<td nowrap="nowrap">${u:formatDate( process.createTime, 'ymdhms' )}</td>
				<td nowrap="nowrap">${u:formatDate( process.closeTime, 'ymdhms' )}</td>
				<td>${ctxProcessTypeMap[process.typeId].title}</td>
				<td>${ctxProcessStatusMap[process.statusId].title}</td>
				<td width="100%">
					<%@ include file="/WEB-INF/jspf/user/process/reference.jsp"%>
				</td>
			</tr>
		</c:forEach>
	</table>
</c:if>

<c:set var="title" value="Мои процессы"/>
<%@ include file="/WEB-INF/jspf/shell_state.jsp"%>
<%@ include file="/WEB-INF/jspf/shell_title.jsp"%>