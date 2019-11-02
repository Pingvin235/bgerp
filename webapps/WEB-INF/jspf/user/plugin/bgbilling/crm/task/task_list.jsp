<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}">
	<table class="mb1">
		<tr>
			<td width="100%">
				<div class="tableIndent">
					<c:url var="createUrl" value="plugin/bgbilling/proto/billingCrm.do">
						<c:param name="action" value="taskGet" />
						<c:param name="billingId" value="${form.param.billingId }" />
						<c:param name="contractId" value="${form.param.contractId }" />
						<c:param name="returnUrl" value="${form.requestUrl}" />
					</c:url> 	
					<button type="button" class="btn-green" onClick="openUrlTo('${createUrl}', $('#${uiid}') )">+</button>
				</div>
			</td>
			<td>
				<form action="plugin/bgbilling/proto/billingCrm.do">
					<input type="hidden" name="action" value="taskList"/>
					<input type="hidden" name="billingId" value="${form.param.billingId}"/>	
					<input type="hidden" name="contractId" value="${form.param.contractId}"/>
					<input type="hidden" name="sort1" value="${form.param.sort1}"/>
					<input type="hidden" name="sort2" value="${form.param.sort2}"/>

					<c:set var="nextCommand" value="; openUrlToParent( formUrl( this.form ), $('#${uiid}') )"/>
					<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
				</form>
			</td>
		</tr>
	</table>

	<table class="data" width="100%">	
		<tr>
			<td></td>
			<td>Код</td>
			<td>Тип</td>
			<td>Статус</td>
			<td>Дата</td>
			<td>Открыта</td>
			<td>Срок</td>
			<td>Адрес</td>
			<td>Телефон</td>
			<td>Группа</td>
            <td>Исполнители</td>
			<td>Комментарий</td>
		</tr>

        <c:set var="maxLength" value="50"/>

		<c:forEach var="task" items="${form.response.data.list}">
			<tr>
				<c:url var="url" value="plugin/bgbilling/proto/billingCrm.do">
					<c:param name="action" value="taskGet"/>
					<c:param name="billingId" value="${form.param.billingId }" />
					<c:param name="contractId" value="${form.param.contractId }" />
					<c:param name="taskId" value="${task.getId()}"/>
                    <c:param name="returnUrl" value="${form.requestUrl}"/>
				</c:url>

                <c:set var="editCommand" value="openUrlTo('${url}',$('#${uiid}').parent())"/>

				<td nowrap="nowrap"><%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%></td>
				
				<td align="center">${task.getId() }</td>
				<td>${task.getTypeTitle() }</td>
				<td align="center">${task.getStatus() }</td>
				<td align="center">${task.getOpenDate() }</td>
				<td>${task.getOpenUser() }</td>
				<td nowrap="nowrap" align="center">${task.getTargetDateTime() }</td>
				<td width="20%">${task.getAddress() }</td>
				<td width="20%">
                    <c:set var="text" value="${task.phones}"/>
                    <%@include file="/WEB-INF/jspf/short_text.jsp"%>
                </td>
				<td width="10%">
                    <c:set var="text" value="${task.groupTitle}"/>
                    <%@include file="/WEB-INF/jspf/short_text.jsp"%>
                </td>
                <td width="10%">
                    <c:set var="text" value="${task.executors}"/>
                    <%@include file="/WEB-INF/jspf/short_text.jsp"%>
                </td>
				<td width="20%">${task.getComment() }</td>
			</tr>
		</c:forEach>
	</table>
</div>
