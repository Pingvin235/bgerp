/*
 * Plugin BGBilling.
 */
"use strict";

$$.bgbilling = new function () {
	const contract = new function () {
		/**
		 * Opens contract in UI.
		 * @param {*} billingId
		 * @param {*} contractId
		 */
		const open = (billingId, contractId) => {
			if ($$.pers['iface.bgbilling.contractOpenMode'] == 2) {
				$$.shell.contentLoad("contract_" + billingId + "#" + contractId);
			} else {
				const url = "/user/plugin/bgbilling/contract.do?billingId=" + billingId + "&id=" + contractId;

				const result = sendAJAXCommand(url);
				if (result.data.customer) {
					const contractTitle = result.data.contract.title;
					const customerId = result.data.customer.id;

					$$.shell.contentLoad("customer#" + customerId).done(() => {
						const $tabs = $("div#customer-" + customerId + " > #customerViewTabs");
						$tabs.tabs("showTab", "bgbilling-contracts");

						// TODO: Wait for contracts tab is loaded.
						$$.ui.tabsLoaded($tabs, "tabsload", function () {
							const $customerContractTabs = $("#bgbilling-customerContractList-" + customerId);
							$$.ui.tabsLoaded($customerContractTabs, "tabsinit", function () {
								if (!$customerContractTabs.tabs("showTab", billingId + "-" + contractId)) {
									// договор возможно "спрятан" под субдоговором - поиск субдоговора по префиксу
									let pos = 0;
									$customerContractTabs.find("ul li").each(function () {
										if (contractTitle.startsWith($(this).find("a").text())) {
											// выделение вкладки субдоговора
											$customerContractTabs.tabs("option", "active", pos);
											// на вкладке субдоговора выделение договора
											const $subContractTabs = $($customerContractTabs.find(">div.ui-tabs-panel")[pos]).find(".ui-tabs");
											$subContractTabs.one("tabsinit", function () {
												$subContractTabs.tabs("showTab", billingId + "-" + contractId);
											});
											return false;
										}
										pos++;
									});
								}
							});
						});
					});
				} else {
					$$.shell.contentLoad("contract_" + billingId + "#" + contractId);
				}
			}
		}

		/**
		 * Loads list of creation tariffs.
		 * @param {*} formId
		 */
		const createTariff = (formId) => {
			const form = document.getElementById(formId);
			const url = form.getAttribute("action") + "?action=contractCreateTariff&typeId=" + form.typeId.value;
			$$.ajax.load(url, $(form.querySelector("#selectTariff")));
		}

		/**
		 * Creates a contract.
		 * @param {*} button
		 * @returns promise.
		 */
		const create = (button) => {
			const form = button.form;

			const def = $.Deferred();

			const tariffId = form.tariffId.value;
			if (tariffId > 0 || tariffId == -1)
				$$.ajax
					.post(form, {control: button})
					.done((result) => {
						def.resolve(result);
					});
			else
				alert('Выберите тариф.');

			return def.promise();
		}

		// public functions
		this.open = open;
		this.createTariff = createTariff;
		this.create = create;
	}

	// public objects
	this.contract = contract;
}

addEventProcessor( 'ru.bgcrm.plugin.bgbilling.event.client.ContractOpenEvent', contractOpenClientEvent );

function contractOpenClientEvent( event )
{
	if( event.className == 'ru.bgcrm.plugin.bgbilling.event.client.ContractOpenEvent' )
	{
		$$.bgbilling.contract.open(event.billingId, event.contractId);
	}
}

function bgbilling_openContract( billingId, contractId ) {
	console.warn($$.deprecated);
	$$.bgbilling.contract.open(billingId, contractId);
}

// загрзука шаблонов договоров в форму создания
function bgbilling_getPatterns( billingId )
{
	var contractPatternList = sendAJAXCommandWithParams('/user/plugin/bgbilling/proto/contract.do?action=bgbillingGetContractPatternList', { 'billingId':billingId  });
	if( contractPatternList.status  == 'ok' )
	{
		var $patternList = $( "#bgbilling-createContractForm select[name = 'patternId']" );
		$patternList.html( "" );

		var options = "";
		$( contractPatternList.data.patterns).each( function(){
			options += optionTag( this.id, this.title + " [" + this.id + "]" );
		});
		$patternList.html( options );
	}
}

function bgbilling_changeContractCustomer( $select, $titleSpan, billingId, contractId, contractTitle )
{
	if( deleteCustomerLinkTo( "contract:" + billingId, contractId ) )
	{
		var customerId = $( "input[name=customerId]", $select ).val();
		var customerTitle = $( ".text-value", $select ).text();

		if( customerId > 0 )
		{
			$titleSpan.html( "<a href='#' onclick='openCustomer( " + customerId + ")';>" +  customerTitle + "</a>" );
			return addCustomerLink( customerId, "contract:" + billingId, contractId, contractTitle );
		}
		else
		{
			$titleSpan.html( "не установлен" );
		}
		return true;
	}
	return false;
}

function bgbilling_selectedRegisterIdChanged()
{
	$('input[name=selectedRegisterPswd]:visible').val("");
	if($('select[name=selectedRegisterId]:visible option:selected').index() == 0)
	{
		$('input[name=selectedRegisterPswd]:visible').attr("disabled","true");
	}
	else
	{
		$('input[name=selectedRegisterPswd]:visible').removeAttr("disabled");
	}
}

function bgbilling_typeListNodeSelected($selectedElement,value)
{
	$selectedElement.closest("form").find( "span" ).css( "font-weight", "" ).css( "color", "" );
	$selectedElement.css( "font-weight", "bold" ).css( "color", "blue" );
	$selectedElement.closest("form").children("input[name=typeId]").val(value);
}

function bgbilling_updateRegisterList( billingId )
{
	var select = $('select[name=selectedRegisterId]:visible');

	var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/bgbilling/proto/cashcheck.do?", { "action" : "registratorList", "billingId": billingId } );
	if( ajaxResponse )
	{
		select.children().remove();
		var registratorList = ajaxResponse.data.registratorList;
		for(var i=0; i<registratorList.length ; i++)
		{
			select.append("<option value="+ registratorList[i].id +">" + registratorList[i].title + "</option>");
		}
		bgbilling_selectedRegisterIdChanged();
	}
}

// создание договора
/*function bgbilling_createContract( form )
{
   var customerId = form.customerId.value;

   var billingId = form.billingId.value;
   var patternId = form.patternId.value;
   var titlePattern = form.titlePattern.value;
   var title = form.title.value;
   var date = form.date.value;

   var url = "/user/plugin/bgbilling/contract.do?action=contractCreate";

   var createResult = sendAJAXCommandWithParams( url, {"billingId" : billingId, "patternId" : patternId, "date" : date, "titlePattern" : titlePattern, "title" : title, "customerId" : customerId } );
   if( !createResult )
   {
	   return;
   }

   var contractId = createResult.data.contract.id;
   var contractTitle =  createResult.data.contract.title;

   // имя контрагента в примечание договора
   if( customerId > 0 )
   {
	   var customerTitle = $(form.customerId).find( "option:selected" ).text();
	   sendAJAXCommandWithParams( "/user/plugin/bgbilling/proto/contract.do?action=UpdateContractTitleAndComment", { 'billingId' : billingId, "contractId" : contractId, "comment" : customerTitle } );
   }

   bgbilling_openContract( billingId, contractId );
}*/

/*function bgbilling_setTitlePattern( form )
{
	var billingId = $(form.billingId ).find( "option:selected" ).attr( "value" );
	var patternId = $(form.patternId).find( "option:selected" ).attr( "value" );

	var url = "/user/plugin/bgbilling/contract.do?action=getContractCreatePattern";
	if( billingId && patternId > 0 )
	{
		var result = sendAJAXCommandWithParams( url, { 'billingId' : billingId, 'patternId' : patternId } );
		if( result )
		{
			$(form).find( "input[name=titlePattern]" ).attr( "value", result.data.value ? result.data.value : "" );
		}
	}
}*/

/*function bgbilling_getContractAddress( contractId, billingId, selectedId )
{
	var $address = $("#"+ billingId+"-"+contractId+"-contractAddressList");
	if( $address.length > 0 )
	{
		var url = "/user/plugin/bgbilling/proto/contract.do?action=addressList&billingId=" + billingId +"&contractId="+contractId;
		openUrlTo( url, $address );
	}
	if( selectedId != 0 )
	{
		$address.find( "option[value=" + selectedId + "]").attr( "selected", "true" );
	}
}*/

/*function bgbilling_dateFromDecriment( dec )
{
	var now = new Date();
	$('input[name=dateFrom]:visible').val($.datepicker.formatDate('dd.mm.yy',new Date(now.getFullYear(),(now.getMonth()-dec) ,1)).toString());
}*/

/*function bgbilling_dateToDecriment( dec )
{
	var now = new Date();
	$('input[name=dateTo]:visible').val($.datepicker.formatDate('dd.mm.yy',new Date(now.getFullYear(),(now.getMonth()-dec+1) ,0)).toString());
}*/

/*function bgbilling_fillByMonthValues(selectorId)
{
	var $select = $("#"+selectorId);
	var now = new Date();
	for(var i=0;i<12;i++)
	{
		$select.append('<option value="' + i + '">' + $.datepicker.formatDate('за MM yy года',now).toString() + '</option>');
		now = new Date(now.getFullYear(),now.getMonth()-1 ,1);
	}
}*/

/*function bgbilling_getLoginPassword(billingId,contractId,login,moduleId )
{
	if($('#'+login+'loginPassword:visible').size() == 0)
	{
		var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/bgbilling/proto/dialup.do?", { "action" : "getLoginPassword","billingId": billingId, "contractId": contractId, "login":login, "moduleId":moduleId } );
		if( ajaxResponse )
		{
			$('#'+login+'loginPassword').html('Пароль доступа:</br>'+ajaxResponse.data.password);
		}
	}
}*/

/*function bgbilling_getContractStatisticPassword(billingId,contractId )
{
	if($('#'+contractId+'statisticPassword:visible').size() == 0)
	{
		var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/bgbilling/proto/contract.do?", { "action" : "getContractStatisticPassword","billingId": billingId, "contractId": contractId } );
		if( ajaxResponse )
		{
			$('#'+contractId+'statisticPassword').html('Пароль доступа к статистике:</br>'+ajaxResponse.data.password);
		}
	}
}*/

/*function bgbilling_getSubContractList( billingId, contractId )
{
	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/contract.do?action=getSubContractList&billingId=" + billingId + "&contractId=" + contractId );
	return result.data.subContractList;
}*/

/*var cerbercrypt = {};

cerbercrypt.getContractCards = function( billingId, moduleId, contractId, includeSlaveCards )
{
	if( includeSlaveCards == undefined )
	{
		includeSlaveCards = 1;
	}

	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/cerbercrypt.do?action=contractCards&contractId=" + contractId + "&billingId=" + billingId + "&includeSlaveCards=" + includeSlaveCards + "&moduleId=" + moduleId );
	return result.data.cards;
}

cerbercrypt.getPackets = function( billingId, moduleId )
{
	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/cerbercrypt.do?action=getPacketList&billingId=" + billingId + "&moduleId=" + moduleId + "&responseType=json" );
	return result.data.packets;
}

cerbercrypt.getContractCardPackets = function( billingId, moduleId, contractId, cardId )
{
	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/cerbercrypt.do?action=cardPacketList&contractId=" + contractId + "&cardId=" + cardId + "&billingId=" + billingId + "&moduleId=" + moduleId + "&responseType=json");
	return result.data.cardPacketList;
}

cerbercrypt.getCards = function( billingId, moduleId )
{
	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/cerbercrypt.do?action=getFreeCards&billingId=" + billingId + "&moduleId=" + moduleId );
	return result.data.cards;
}

cerbercrypt.updateCard = function( billingId, moduleId, contractId, cardNumber, baseCardId, dateFrom, dateTo )
{
	var request = "/user/plugin/bgbilling/proto/cerbercrypt.do?action=updateCard&billingId=" + billingId + "&cardNumber=" + cardNumber + "&contractId=" + contractId + "&baseCardId=" + baseCardId + "&moduleId=" + moduleId + "&dateFrom=" + dateFrom;
	sendAJAXCommand( request );
}

cerbercrypt.updateCardPacket = function( billingId, moduleId, contractId, cardNumber, packetId  )
{
	var request = "/user/plugin/bgbilling/proto/cerbercrypt.do?action=updateCardPacket&billingId=" + billingId + "&cardNumber=" + cardNumber + "&contractId=" + contractId + "&packetId=" + packetId + "&moduleId=" + moduleId;
	sendAJAXCommand( request );
}

cerbercrypt.closeCardPacket = function( billingId, moduleId, contractId, cardNumber, id, packetId, closeDate )
{
	var request = "/user/plugin/bgbilling/proto/cerbercrypt.do?action=updateCardPacket&billingId=" + billingId + "&cardNumber=" + cardNumber + "&contractId=" + contractId + "&id=" + id + "&moduleId=" + moduleId;
	request += "&packetId=" + packetId;
	if( closeDate != undefined )
	{
		request += "&date2=" + closeDate;
	}
	sendAJAXCommand( request );
}*/

/*var voip = {};

voip.getLogins = function( billingId, contractId )
{
	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/voip.do?action=getLogins&billingId=" + billingId + "&contractId=" + contractId );
	return result.data.logins;
}

voip.getLogin = function( billingId, loginId )
{
	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/voip.do?action=getLogin&billingId=" + billingId + "&loginId=" + loginId );
	return result.data.login;
}

voip.generatePassword = function( length )
{
	var text = "";
	var possible = "0123456789";

	for( var i=0; i < length; i++ )
	{
		text += possible.charAt( Math.floor(Math.random() * possible.length) );
	}
	return text;
}

voip.updateLogin = function( billingId, contractId, loginId, alias, objectId, comment, type, dateFrom, dateTo, loginPassword, setPassword )
{
	var request = "/user/plugin/bgbilling/proto/voip.do?action=updateLogin";
	request += "&billingId=" + billingId;
	request += "&loginId=" + loginId;
	request += "&contractId=" + contractId;
	request += "&alias=" + alias;
	request += "&objectId=" + objectId;
	request += "&comment=" + comment;
	request += "&type=" + type;
	request += "&dateFrom=" + dateFrom;
	request += "&dateTo=" + dateTo;

	if( setPassword == true )
	{
		request += "&setPassword=1";
	}

	if( loginPassword != null )
	{
		request += "&loginPassword=" + loginPassword;
	}
	else
	{
		request += "&loginPassword=" + voip.generatePassword( 10 );
	}

	var result = sendAJAXCommand( request );
	return result.data.login;
}*/
