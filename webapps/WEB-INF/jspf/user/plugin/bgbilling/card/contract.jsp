<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="uiid" value="${u:uiid()}" />


<html:form action="/user/plugin/bgbilling/proto/card.do"
	styleId="${uiid}">
	<input type="hidden" name="method" value="activateCard" />
	<html:hidden property="contractId" value="${form.param.contractId}" />
	<html:hidden property="billingId" />
	<html:hidden property="moduleId" />
	<html:hidden property="id" />
	<html:hidden property="serviceId" />



	<c:url var="url" value="/user/plugin/bgbilling/proto/card.do">
		<c:param name="method" value="contractInfo" />
		<c:param name="billingId" value="${form.param.billingId}" />
		<c:param name="moduleId" value="${form.param.moduleId}" />
		<c:param name="contractId" value="${form.param.contractId}" />
	</c:url>

	<table>
		<tr class="in-pl1">
			<td>
				<h2>Код (логин) карты:</h2> <input type="text" style="width: 100%"
				name="cardCode" value="${form.param.cardCode}" id="${uiid}_card" />
			</td>
			<td>
				<h2>PIN-код (пароль):</h2> <input type="text" name="pinCode"
				style="width: 100%" value="${form.param.pinCode}" id="${uiid}_pin" />
			</td>
			<td>
				<h2>&nbsp;</h2>
				<button type="button" class="btn-grey ml1"
					onclick="$$.ajax.load(this.form, $('#${uiid}').parent());">Активировать</button>
			</td>
		</tr>
	</table>

	<c:set var="sUrl">'${url}'+'&serviceId='+$("input[name='sid']").val()+'&cardCode='+$('#${uiid}_card').val()+'&pinCode='+$('#${uiid}_pin').val()</c:set>
	<c:set var="script">$$.ajax.load(${sUrl}, $('#${uiid}').parent())</c:set>
	<table class="hdata mt1 hl">
		<tr>
			<td class="header" colspan="4"">Тип карт: <ui:combo-single
					name="sid" value="${form.param.serviceId}"
					list="${frd.serviceList}" onSelect="${script}" /></td>
		</tr>
		<tr>
			<td class="header">Статус</td>
			<td class="header">Дата активации</td>
			<td class="header">Сумма</td>
			<td class="header">Номер карты</td>
		</tr>
		<c:forEach var="item" items="${frd.cardList}">
			<tr>
				<td>${item.status}</td>
				<td>${item.activationDate}</td>
				<td>${item.summa}</td>
				<td>${item.number}</td>
			</tr>
		</c:forEach>
	</table>

</html:form>



