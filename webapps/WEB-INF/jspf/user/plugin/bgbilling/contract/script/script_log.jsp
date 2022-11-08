<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<form action="/user/plugin/bgbilling/proto/contract.do">
	<input type="hidden" name="action" value="scriptLog" />
	<input type="hidden" name="billingId" value="${form.param.billingId}" />
	<input type="hidden" name="contractId" value="${form.param.contractId}" />

	c
	<c:set var="editable" value="true"/>
	<input type="text" name="dateFrom" value="${form.param.dateFrom}" id="${uiid}-dateFrom"/>
	<c:set var="selector" value="#${uiid}-dateFrom"/>
	<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>
	по
	<c:set var="editable" value="true"/>
	<input type="text" name="dateTo" value="${form.param.dateTo}" id="${uiid}-dateTo"/>
	<c:set var="selector" value="#${uiid}-dateTo"/>
	<%@ include file="/WEB-INF/jspf/datetimepicker.jsp"%>

	<c:set var="nextCommand" value="; $$.ajax.load(this.form, $(this.form).parent());" scope="request"/>
	<button type="button" class="btn-grey ml1" onclick="${nextCommand}">${l.l('Вывести')}</button>
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</form>

<table class="data mt1" width="100%">
	<tr>
		<td width="30"></td>
		<td>Время</td>
		<td width="100%">Название</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<td><button type="button" class="btn-white btn-small" onclick="$(this).parent().parent().next().toggle()">Просмотр</button></td>
			<td nowrap="nowrap">${item.time}</td>
			<td>${item.title}</td>
		</tr>
		<tr style="display:none">
			<td colspan=3>${fn:replace(item.data,'u000A','</br>')}</td>
		</tr>
	</c:forEach>
</table>