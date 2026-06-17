<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}"/>
<div id="${uiid}">
	<html:hidden property="streetId"/>
	<html:hidden property="houseId"/>
	<input type="text" name="street" value="${form.param.street}" placeholder="${l.l('Улица')}" title="${l.l('Улица')}" size="20"/>
	<input type="text" name="house" value="${form.param.house}" placeholder="${l.l('Дом/дробь')}" title="${l.l('Дом/дробь')}" size="4" class="ml05"/>
	<input type="text" name="flat" value="${form.param.flat}" placeholder="${l.l('Квартира')}" title="${l.l('Квартира')}" size="4" class="ml05"/>
</div>

<script>
	$(function() {
		addAddressSearch("#${uiid}");
	})
</script>