<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<td colspan="7">
	<c:set var="process" value="${form.response.data.process}"/>
	
	<c:set var="priority" value="${process.priority}"/>
	<%@ include file="/WEB-INF/jspf/process_color.jsp"%>
	
	<h2>${l.l('Процесс')}</h2>
	<h2 style="background-color:${color};">${process.typeTitle} (<a href="#UNDEF" onclick="openProcess(${process.id}); return false;">${process.id}</a>)</h2> 
	<table class="oddeven" style="width: 100%;">
		<tr>
			<td nowrap="nowrap">${l.l('Создан (статус)')}</td>
			<td width="100%">${u:formatDate( process.createTime, 'ymdhms' )} ( ${process.statusTitle} )</td>
		</tr>
		<tr>
			<td nowrap="nowrap">${l.l('Закрыт')}</td>
			<td width="100%">${u:formatDate( process.closeTime, 'ymdhms' )}</td>
		</tr>
		<tr>
			<td nowrap="nowrap">${l.l('Исполнители')}</td>
			<td width="100%">${u:objectTitleList( ctxUserList, process.executorIds )}</td>
		</tr>
		<tr>
			<td nowrap="nowrap">${l.l('Отделы')}</td>
			<td width="100%">${u:objectTitleList( ctxUserGroupList, process.groupIds )}</td>
		</tr>
		<tr>
			<td colspan="2">
				<textarea rows="7" cols="120" style="min-width:100%">${process.description}</textarea>
			</td>
		</tr>
		<tr>
			<td colspan="2" style="background-color:#eeeeee;">
				<h2>ИСТОРИЯ ИЗМЕНЕНИЯ СТАТУСА</h2>
				<table style="width: 100%;" class="data">
					<tr>
						<td>${l.l('Статус')}</td>
						<td>${l.l('Комментарий')}</td>
						<td>${l.l('Дата')}</td>
						<td>${l.l('Пользователь')}</td>
					</tr>
					<c:forEach var="item" items="${form.response.data.list}">
						<tr>
							<td align="center">${item.statusTitle}</td>
							<td>${item.comment}</td>
							<td align="center" nowrap="nowrap">${u:formatDate( item.date, 'ymdhms' )}</td>
							<td align="center" nowrap="nowrap">${item.userTitle}</td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>

</td>