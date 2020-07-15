<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<html:form action="/user/plugin/bgbilling/proto/rscm" styleId="${uiid}">
	<html:hidden property="billingId"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>
	<input type="hidden" name="action" value="serviceList"/>
	
	<c:url var="url" value="/user/plugin/bgbilling/proto/rscm.do">
		<c:param name="action" value="serviceGet"/>
		<c:param name="contractId" value="${form.param.contractId}"/>
		<c:param name="billingId" value="${form.param.billingId}"/>
		<c:param name="moduleId" value="${form.param.moduleId}"/>
		<c:param name="returnUrl" value="${form.requestUrl}"/>
	</c:url>
	
	<button type="button" class="btn-green mr1" title="Добавить услугу" onclick="openUrlToParent('${url}', $('#${uiid}') )">+</button>
	
	<div id="dateFilter" style="display: inline-block;">
		<ui:date-month-days/>
	</div>

	<c:set var="sendForm">openUrlToParent( formUrl( $('#${uiid}') ), $('#${uiid}') );</c:set>
	
	<button type="button" class="ml2 btn-grey" onclick="${sendForm}" title="Вывести">=&gt;</button>
	
	<c:set var="nextCommand" value=";${sendForm}" scope="request"/>
	<%@ include file="/WEB-INF/jspf/page_control.jsp"%>
</html:form>

<table class="data" style="width: 100%;" id="${uiid}">
	<tr>
		<td width="30"></td>
		<td>Дата</td>
		<td width="50%">Услуга</td>
		<td>Кол-во</td>
		<td>Ед. имерения</td>		
		<td width="50%">Комментарий</td>
	</tr>
	<c:forEach var="item" items="${form.response.data.list}">
		<tr>
			<c:url var="editU" value="${url}">
				<c:param name="id" value="${item.id}"/>			
			</c:url> 
			<c:set var="editCommand" value="openUrlToParent('${editU}', $('#${uiid}') )"/>
			
			<c:url var="deleteAjaxUrl" value="/user/plugin/bgbilling/proto/rscm.do">
				<c:param name="action" value="serviceDelete"/>
				<c:param name="contractId" value="${form.param.contractId}"/>
				<c:param name="billingId" value="${form.param.billingId}"/>
				<c:param name="moduleId" value="${form.param.moduleId}"/>
				<c:param name="id" value="${item.id}"/>
				<c:param name="month" value="${u:formatDate(item.date, 'ymd')}"/>
			</c:url>
			<c:set var="deleteAjaxCommandAfter" value="openUrlToParent('${form.requestUrl}',$('#${uiid}'))"/>
			
			<td nowrap="nowrap">
				<%@ include file="/WEB-INF/jspf/edit_buttons.jsp"%>
			</td>
			<td nowrap="nowrap">${u:formatDate(item.date, 'ymd')}</td>
			<td>${item.serviceTitle}</td>
			<td>${item.amount}</td>
			<td>${item.unit}</td>
			<td>${item.comment}</td>
		</tr>
	</c:forEach>
</table>