<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<%--
url - вызываемый URL, к которому в начало будет добавлено plugin/bgbilling/proto/ и в конце параметры billingId, contractId
command - вызываемый после URL JS
level - отступ от края 0, 1
title - подпись
value - значение в правой области
rowId - ID tr
valueId - ID узла с value
--%>

<c:if test="${empty level}">
	<c:set var="level" value="1"/>
</c:if>

<c:set var="rowAttrs">id="${rowId}" class="${rowClass}"</c:set>

 <c:choose>
 	<c:when test="${url eq 'no'}">
		<tr ${rowAttrs}>
	</c:when>
	<c:when test="${url eq 'dev'}">
		<tr style="cursor: pointer; color: blue;" clientPackage="${item.clientPackage}" onClick="markOutTr(this); alert('Функционал в разработке.');" ${rowAttrs}>
	</c:when>
	<c:when test="${not empty url}">
		<c:url var="billingActionUrl" value="/user/plugin/bgbilling/proto/${url}">
			<c:param name="billingId" value="${contract.billingId}"/>
			<c:param name="contractId" value="${contract.id}"/>
		</c:url>
		<tr style="cursor: pointer;" onClick="markOutTr(this); $$.ajax.load('${billingActionUrl}', $('#${contractTreeId} #content'));" ${rowAttrs}>
	</c:when>
	<c:otherwise>
		<tr ${rowAttrs}>
	</c:otherwise>
</c:choose>
			<c:if test="${empty value}">
				<c:set var="colspan" value="colspan='2'"/>
			</c:if>
			<td ${colspan}>
				<div class="pl${level} row">
					${title}
				</div>
			</td>
			<c:if test="${not empty value}">
				<td id="${valueId}">
					${value}
				</td>
			</c:if>
		</tr>
