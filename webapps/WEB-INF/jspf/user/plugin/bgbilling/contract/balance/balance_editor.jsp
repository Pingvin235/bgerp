<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<c:set var="balanceItem" value="${frd.balanceItem}"/>

<c:set var="balanceForm" value="${u:uiid()}"/>

<form action="/user/plugin/bgbilling/proto/balance.do" id="${balanceForm}">
	<input type="hidden" name="method" value="updateBalance" />
	<input type="hidden" name="billingId" value="${form.param.billingId}" />
	<input type="hidden" name="contractId" value="${form.param.contractId}" />
	<input type="hidden" name="item" value="${form.param.item}" />
	<input type="hidden" name="id" value="${balanceItem.id}" />
	<input type="hidden" name="typeId" value="" />

	<h1>Редактор</h1>
	<div style="display:table; width:100%" class="mb1">
		Сумма
		<input type="text" name="summa" value="${balanceItem.sum}" style="text-align:center;"/>
		Дата
		<c:set var="date" value="${frd.date}"/>
		<c:if test="${not empty balanceItem.date}">
			<c:set var="date" value="${tu.format(balanceItem.date, 'dd.MM.yyyy')}"/>
		</c:if>
		<ui:date-time name="date" value="${date}" styleClass="mr1"/>

		<c:set var="dbInfo" value="${ctxPluginManager.pluginMap['bgbilling'].dbInfoManager.dbInfoMap[form.param.billingId]}"/>
		<c:if test="${dbInfo.pluginSet.contains('ru.bitel.bgbilling.plugins.cashcheck')}">
			<c:set var="mode" value="${dbInfo.guiConfigValues.get('cashcheck.user.'.concat(ctxUser.login).concat('.checkbox.mode'))	}"/>
			<c:if test="${empty mode}">
				<c:set var="mode" value="${dbInfo.guiConfigValues.get('cashcheck.default.checkbox.mode')}"/>
			</c:if>

			<c:if test="${mode eq 'on' or mode eq 'defaulton'}">
				<c:set var="checked" value="checked"/>
			</c:if>
			<c:if test="${mode eq 'on' or mode eq 'off'}">
				<c:set var="disabled" value="disabled"/>
			</c:if>

			<div style="display: inline-block;">
				Печатать чек (ККМ: ${frd.currentPrinter.title})
				<input type="checkbox" name="printCheck" ${checked} ${disabled}/>
			</div>
		</c:if>
	</div>

	Тип:
	<div class="box p05 mb1" style="overflow: auto; width: inherit; height: 200px;background-color: #ffffff; cursor: pointer;">
		<c:forEach var="type" items="${frd.itemTypes.children}">
			<c:set var="node" value="${type}" scope="request"/>
			<c:set var="level" value="0" scope="request"/>
			<jsp:include page="type_list.jsp"/>
		</c:forEach>
		<c:if test="${not empty balanceItem.getType()}">
			<script>
				$(function()
				{
					$('#${balanceForm} #${balanceItem.getType()}_title').parent().parent().css('display','block');
					$('#${balanceForm} #${balanceItem.getType()}_title').click();
				})
			</script>
		</c:if>
	</div>

	Комментарий:
	<textarea rows="2" cols="300" style="width:100%;" name="comment" class="mb1">${balanceItem.getComment()}</textarea>

	<c:set var="printCheckForm" value="${form.param.billingId}-${form.param.contractId}-printCheck-form"/>
	<button type="button" class="btn-grey" onclick="
			$$.ajax.post(this.form).done((result) => {
				if($(this.form).find('input[name=printCheck]').is(':checked'))
				{
					$('#${printCheckForm} input[name=paymentId]').val(result.data.id);
					$('#${printCheckForm} input[name=clientCash]').val(this.form.summa.value);
					$('#${printCheckForm}').dialog( 'open' );
				}
				$$.ajax.load('${form.param.returnUrl}', $(this.form).parent());
			})">OK</button>
	<button type="button" class="btn-white ml1" onClick="$$.ajax.load('${form.param.returnUrl}', $(this.form).parent());">Отмена</button>
</form>