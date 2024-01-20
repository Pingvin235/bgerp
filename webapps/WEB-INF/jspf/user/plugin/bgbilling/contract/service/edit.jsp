<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>

<c:set var="pair" value="${frd.pair}"/>

<h1>Редактор</h1>

<html:form action="/user/plugin/bgbilling/proto/contract" styleId="${uiid}" style="height: 400px;">
	<input type="hidden" name="action" value="serviceUpdate"/>
	<html:hidden property="id"/>
	<html:hidden property="moduleId"/>
	<html:hidden property="contractId"/>
	<html:hidden property="billingId"/>

	<div style="display: inline-block; width: 50%; vertical-align: top;">
		<h2>Услуги</h2>
		<c:choose>
			<c:when test="${form.id le 0}">
				<ui:select-mult list="${pair.second}" hiddenName="serviceId" style="width: 100%;" showId="1"/>
			</c:when>
			<c:otherwise>
				<input type="hidden" name="serviceId" value="${pair.first.serviceId}"/>
				<c:forEach var="item" items="${pair.second}">
					<c:if test="${item.id eq pair.first.serviceId}">
						<c:set var="serviceTitle" value="${item.title}"/>
					</c:if>
				</c:forEach>
				<div class="tt">${serviceTitle}</div>
			</c:otherwise>
		</c:choose>
	</div><%--
--%><div style="display: inline-block; width: 50%;" class="pl1">
		<h2>Период</h2>
		<div>
			<ui:date-time paramName="dateFrom" value="${tu.format(pair.first.dateFrom, 'dd.MM.yyyy')}"/>
			-
			<ui:date-time paramName="dateTo" value="${tu.format(pair.first.dateTo, 'dd.MM.yyyy')}"/>
		</div>

		<h2>Комментарий</h2>
		<textarea name="comment" style="width: 100%; resize: none;" class="layout-height-rest">${pair.first.comment}</textarea>
	</div>

	<%@ include file="/WEB-INF/jspf/ok_cancel_in_form.jsp"%>
</html:form>

