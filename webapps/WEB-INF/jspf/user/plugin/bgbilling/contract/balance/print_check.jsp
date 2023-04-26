<%@ page contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/jspf/taglibs.jsp"%>

<div id="${form.param.billingId}-${form.param.contractId}-printCheck-form" title="Печать чека" style="display: none;">
	<form>
		<input type="hidden" name="paymentId"/>
		<b>Регистрация на КММ:</b>
		<fieldset>
			<select name="selectedRegisterId" style="width:100%" onchange="bgbilling_selectedRegisterIdChanged();">
			</select>
			<script>
				bgbilling_updateRegisterList('${form.param.billingId}');
			</script>
			Пароль
			<input type="password" name="selectedRegisterPswd" style="width:100%" disabled/>
		</fieldset>
		</br>
		<b>Сумма от клиента:</b>
		 <fieldset>
			<input type="text" name="clientCash" value="" style="text-align:center; width:100%"/>
		</fieldset>
	</form>
</div>

<script>
	 $( "#${form.param.billingId}-${form.param.contractId}-printCheck-form" ).dialog({
		 	disabled:true,
			autoOpen: false,
			height: 300,
			width: 350,
			modal: true,
			open: function(event, ui) {
					bgbilling_updateRegisterList('${form.param.billingId}');
					var form = '${form.param.billingId}-${form.param.contractId}-printCheck-form';
					$('#'+form+' input[name=clientCash]').focus().select();
					$('#'+form+' input[name=selectedRegisterPswd]').val("");
				},
			buttons: {
				"Печать": function() {
					var form = '${form.param.billingId}-${form.param.contractId}-printCheck-form';
					var paymentId = $('#'+form+' input[name=paymentId]').val();
					var selectedRegisterId = $('#'+form+' option:selected').val();
					var selectedRegisterPswd = $('#'+form+' input[name=selectedRegisterPswd]').val();
					var clientCash = $('#'+form+' input[name=clientCash]').val();

					const url = "/user/plugin/bgbilling/proto/cashcheck.do?" + $$.ajax.requestParamsToUrl({"action" : "printCheck", "billingId": "${form.param.billingId}", "paymentId":paymentId, "clientCash":clientCash, "selectedRegisterId": selectedRegisterId, "selectedRegisterPswd": selectedRegisterPswd});
					var result = sendAJAXCommand(url);
					if(result)
					{
						alert("Напечатан чек на сумму "+ result.data.summa + "\n" + "сдача "+ result.data.submit);
					}
					else
					{
						alert("Внимание чек НЕ напечатан!");
					}
					$( this ).dialog( "close" );
				},
				"Отмена": function() {
					$( this ).dialog( "close" );
					alert("Внимание чек НЕ напечатан!");
			}
		}
	});
</script>