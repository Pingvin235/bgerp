<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="type" value="${form.response.data.type}"/>

<input type="hidden" name="billingId" value="${type.billingId}"/>
<input type="hidden" name="patternId" value="${type.patternId}"/>

<ui:combo-single hiddenName="tariffId" list="${type.tariffList}" prefixText="Тариф:" style="width: 100%;">
	<jsp:attribute name="valuesHtml">
		<li value="-1">-- без указания тарифа --</li>
	</jsp:attribute>
</ui:combo-single>
