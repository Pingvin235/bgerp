<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<h2 id="${uiid}">Логины</h2>

<c:url var="url" value="plugin/bgbilling/proto/dialup.do">
	<c:param name="action" value="getLogin"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="contractId" value="${form.param.contractId}"/>	
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url> 

<button type="button" class="btn-green" onclick="openUrlToParent( '${url}', $('#${uiid}') )">+</button>

<table class="data mt1" width="100%">
	<tr>
		<td></td>
		<td>Логин</td>
		<td>Алиас</td>
		<td>Период</td>
		<td>Доступ</td>
		<td width="100%">Комментарий</td>
	</tr>
	<c:forEach var="login" items="${form.response.data.loginList}">
		<tr>
			<c:url var="eUrl" value="${url}">
				<c:param name="id" value="${login.id}"/>
			</c:url>
			<c:set var="editCommand" value="openUrlToParent('${eUrl}', $('#${uiid}') )"/>
			
			<c:url var="deleteAjaxUrl" value="plugin/bgbilling/proto/dialup.do">
				<c:param name="action" value="deleteLogin"/>
				<c:param name="contractId" value="${form.param.conractId}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="id" value="${login.id}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td>${login.login}</td>
			<td>${login.alias}</td>
			<td nowrap="nowrap">${u:formatPeriod( login.dateFrom, login.dateTo, 'ymd' )}</td>
			<td>${login.statusTitle}</td>
			<td>${login.comment}</td>
		</tr>
	</c:forEach>
</table>

<h2>Учётные периоды</h2>

<c:url var="url" value="/user/plugin/bgbilling/proto/dialup.do">
	<c:param name="action" value="getPeriod"/>
	<c:param name="billingId" value="${form.param.billingId}"/>
	<c:param name="moduleId" value="${form.param.moduleId}"/>
	<c:param name="contractId" value="${form.param.contractId}"/>
	<c:param name="returnUrl" value="${form.requestUrl}"/>
</c:url>

<button type="button" class="btn-green" onclick="openUrlToParent( '${url}', $('#${uiid}') )">+</button>

<div class="layout-height-rest" style="overflow: auto;">
	<table class="data mt1" width="100%">
		<tr>
			<td></td>
			<td width="50%">Начало</td>
			<td width="50%">Окончание</td>
		</tr>
		<c:forEach var="period" items="${form.response.data.periodList}">
			<tr>
				<c:url var="eUrl" value="${url}">
					<c:param name="id" value="${period.id}"/>
				</c:url>
				<c:set var="editCommand" value="openUrlToParent('${eUrl}', $('#${uiid}') )"/>
				
				<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/dialup.do">
					<c:param name="action" value="deletePeriod"/>
					<c:param name="contractId" value="${form.param.conractId}"/>
					<c:param name="id" value="${period.id}"/>
				</c:url>
				<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
				<td nowrap="nowrap">
					<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
				</td>
				<td>${period.periodFrom}</td>
				<td>${period.periodTo}</td>
			</tr>
		</c:forEach>		
	</table>
</div>

<%@ include file="/WEB-INF/jspf/layout_process.jsp"%>