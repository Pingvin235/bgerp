$$.bgbilling = new function() {
	const contractOpen = (billingId, contractId) => {
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

	// public functions
	this.contractOpen = contractOpen;
}


addEventProcessor( 'ru.bgcrm.plugin.bgbilling.event.ContractOpenEvent', contractOpenClientEvent );

function contractOpenClientEvent( event )
{
	if( event.className == 'ru.bgcrm.plugin.bgbilling.event.ContractOpenEvent' )
	{
		bgbilling_openContract( event.billingId, event.contractId  );
	}
}

function bgbilling_openContract( billingId, contractId ) {
	console.warn($$.deprecated);
	$$.bgbilling.contractOpen(billingId, contractId);
}

/* Incorrect, but common contracts are deprecated anyway.
function bgbilling_openCommonContract( commonContractId )
{
	var url = "/user/plugin/bgbilling/commonContract.do?id=" + commonContractId;
	
	var result = sendAJAXCommand( url );
	if( result.data.contract )
	{
		var contractTitle = result.data.contract.formatedNumber;
		var customerId = result.data.contract.customerId
		
		contentLoad( "customer#" + customerId );
		
		$("div#customer-" + customerId + " > #customerViewTabs" ).tabs( "showTab", "bgbilling-contracts" );
		
		var $customerContractList = $("#bgbilling-customerContractList-" + customerId );
		
		// договор возможно "спрятан" под субдоговором - поиск субдоговора по префиксу
		var pos = 0;
		$customerContractList.find( "ul li" ).each( function()
		{
			if( contractTitle == $(this).find( "a" ).text() )
			{
				// выделение вкладки субдоговора
				$customerContractList.tabs( "option", "active", pos );				
				return false;
			}
			pos++;
		});
	}	
}
*/

//отправка AJAX команды, с запросом к биллингу
//возвращается XML документ, полученный от биллинга
/*
function bgbilling_urlCall( url, toPostNames )
{
	var result = false;
	
	var separated = separatePostParams( url, "", toPostNames );
	url = separated.url;
	var data = separated.data;	
	
	$.ajax({
		type: "POST", 
		url: url,
		data : data,
		async : false,
		dataType: "xml",
		success: function( data )
		{
			var status = $( data ).find( "data:first" ).attr( "status" ); 
			if(	status != 'ok' )
			{
				alert( $( data ).find( "data:first" ).text() );
			}
			else
			{
				result = data.documentElement;
			}
		},
		error: function( response ) 
		{
			alert( "AJAX error!" );
		}	
	});
	
	return result;
}
*/

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

// создание договора
function bgbilling_createContract( form )
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

function bgbilling_setTitlePattern( form )
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
}
function bgbilling_editProblem( url, billingId )
{
	if( !billingId || billingId == -1 )
	{
		alert( "Не выбран биллинг." );
		return;
	}
	
	openUrlContent(url);
}


/*function bgbilling_editProblem( uiid, returnUrl, billingId, processId, problemId, processDescription )
{
	if( !billingId || billingId == -1 )
	{
		alert( "Не выбран биллинг." );
		return;
	}
	
	var $editor = $( uiid + " #editor" );
	
	$( uiid + " #table" ).hide();
	$editor.show();
	
	var url =
		"plugin/bgbilling/billing.do?forwardFile="  + encodeURIComponent( "/WEB-INF/jspf/user/plugin/bgbilling/crm/problem_editor_form.jsp" ) +
		"&returnUrl=" + encodeURIComponent( returnUrl ) +
		"&id=" + processId +
		"&description=" + processDescription +
		requestParamsToUrl({ "uiid" : uiid,
							 "billingId" : billingId }) +
		requestParamsToUrl({ "id" : "new" }, 'billing');
	
	if( problemId > 0 )
	{
		url = 
			"plugin/bgbilling/billing.do?forwardFile=" + encodeURIComponent( "/WEB-INF/jspf/user/plugin/bgbilling/crm/problem_editor.jsp" ) +
			"&returnUrl=" + encodeURIComponent( returnUrl ) +
			"&id=" + processId +
			requestParamsToUrl({ "uiid" : uiid, 
								 "billingId" : billingId }) +
			requestParamsToUrl({"module" : "ru.bitel.bgbilling.plugins.crm", 
								"action" : "GetRegisterProblem",
								"id" : problemId }, 
								'billing');
	}
	
	openUrlTo( url, $editor, {"toPostNames" : ['returnUrl', 'description']} );
}

/*function bgbilling_registerGroupChanged( uiid, billingId, groupId, executors )
{
	var $executors = $( uiid + " #editor #executors" );

	var url = 
		"plugin/bgbilling/billing.do?" +
		"forwardFile=" + encodeURIComponent( "/WEB-INF/jspf/user/plugin/bgbilling/crm/register_executors.jsp" ) +
		requestParamsToUrl({ "billingId" : billingId,
			 				 "executors" : executors }) +
		requestParamsToUrl({ "module" : "ru.bitel.bgbilling.plugins.crm", 
							 "action" : "RegisterExecutorList",
							 "groups" :  groupId }, 'billing');
	openUrlTo( url, $executors );
}*/

function bgbilling_registerProblemUpdate( url, processId, billingId, problemId )
{
	var result = sendAJAXCommand( url ) 
	if( result )
	{
		// создание проблемы
		if( problemId == "new" || problemId == "")
		{
			problemId = result.data.problem;
		
			// привязка проблемы биллинга к процессу
			addLink( "process", processId, "bgbilling-problem:" + billingId, problemId, "" );
		}
		
		refreshCurrentSelectedTab();
	}
}

/* 
function bgbilling_registerTaskCreate( billingId, contractId, contractId, typeId, addressParamId, groupId, statusId, description )
{
	var result =
		bgbilling_billingAction( billingId, 
				{ "module" : "ru.bitel.bgbilling.plugins.crm",
				  "action" : "UpdateRegisterTask",
				  "id" : "new",
				  "cid" :contractId,
				  "type" : typeId,
				  "apid" : addressParamId,
				  "group" : groupId,
				  "status" : statusId,
				  "comment" : description });
	
	return $(result).find( "task:first" ).attr( "id" );
}

function bgbilling_findContract( billingId, commonContractId, serviceCode )
{
	return sendAJAXCommandWithParams( "/user/plugin/bgbilling/contract.do?action=contractFind", 
			{"billingId" : billingId, "commonContractId" : commonContractId, "serviceCode" : serviceCode } );
}
*/

function bgbilling_getRegistredGroups( itemId, billingId, selectedId )
{
	var $groups = $("#"+ billingId+"-"+itemId+"-registerGroupList");
	if( $groups.length > 0 )
	{
		var url = "/user/plugin/bgbilling/proto/billingCrm.do?action=registerGroupList&billingId=" + billingId +"&contractId="+itemId;
		openUrlTo( url, $groups );
	}
	
	if( selectedId != 0 )
	{
		$groups.find( "option[value=" + selectedId + "]").attr( "selected", "true" );
	}
}

function bgbilling_getRegistredExecutors( $selector, billingId, groupId, selectedIds )
{
	var $executors = $("#"+ $selector);
	if( $executors.length > 0 )
	{
		var url = "/user/plugin/bgbilling/proto/billingCrm.do?action=registerExecutorList&billingId=" + billingId + "&groupId="+groupId;
		openUrlTo( url, $executors );
	}
	if( selectedIds )
	{
		for( var i = 0; i < selectedIds.length; i++ )
		{
			$executors.find( ":checkbox[value=" + selectedIds[i] + "]").attr( "checked", "1" );
		}
	}
}

function bgbilling_getContractAddress( contractId, billingId, selectedId )
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
}

function bgbilling_getTaskTypes( contractId, billingId, selectedId )
{
	var $taskTypes = $("#"+ billingId+"-"+contractId+"-taskTypeList");
	if( $taskTypes.length > 0 )
	{
		var url = "/user/plugin/bgbilling/proto/billingCrm.do?action=taskTypeList&billingId=" + billingId +"&contractId="+contractId;	
		openUrlTo( url, $taskTypes );
	}
	if( selectedId != 0 )
	{
		$taskTypes.find( "option[value=" + selectedId + "]").attr( "selected", "true" );
	}
}

function bgbilling_updateTaskStatus( taskStatusId )
{
	$('select[name=statusId]:visible').find( "option[value=" + taskStatusId + "]").attr( "selected", "true" );
}

function bgbilling_updateGroupId(billingId, contractId, typeId)
{
	var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/bgbilling/proto/billingCrm.do?", { "action" : "getRegisterSubjectGroup", "typeId": typeId , "billingId": billingId } );
	if( ajaxResponse )
	{ 
		$('#'+billingId+'-'+contractId+'-createCallForm select[name=registerGroupId] option[value='+ajaxResponse.data.groupId+']').attr("selected","true");
	};
}

function bgbilling_dateFromDecriment( dec )
{
	var now = new Date(); 
	$('input[name=dateFrom]:visible').val($.datepicker.formatDate('dd.mm.yy',new Date(now.getFullYear(),(now.getMonth()-dec) ,1)).toString());
}

function bgbilling_dateToDecriment( dec )
{
	var now = new Date(); 
	$('input[name=dateTo]:visible').val($.datepicker.formatDate('dd.mm.yy',new Date(now.getFullYear(),(now.getMonth()-dec+1) ,0)).toString());
}

function bgbilling_fillByMonthValues(selectorId)
{
	var $select = $("#"+selectorId);
	var now = new Date();
	for(var i=0;i<12;i++)
	{
		$select.append('<option value="' + i + '">' + $.datepicker.formatDate('за MM yy года',now).toString() + '</option>');
		now = new Date(now.getFullYear(),now.getMonth()-1 ,1);
	}
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
/*
function bgbilling_fillModuleList(billingId)
{
	var select = $('select[id=moduleId]:visible');
	
	var ajaxResponse = sendAJAXCommandWithParams( "plugin/bgbilling/proto/billingCrm.do?", { "action" : "getBillingModuleList", "billingId": billingId } );
	if( ajaxResponse )
	{
		select.children().remove();
		var moduleList = ajaxResponse.data.moduleList;
		select.append("<option value=-1 selected>любой модуль</option>");
		for(var i=0; i<moduleList.length ; i++)
		{
			select.append("<option value="+ moduleList[i].id +">" + moduleList[i].title + "</option>");
		}
	}
}

function bgbilling_moduleIdChanged(billingId, contractId, moduleId, tariffItemId, useFilter, showUsed, selectedId)
{
	if(typeof moduleId == 'undefined')
	{
		moduleId = -1;
	}
	if(typeof tariffItemId == 'undefined')
	{
		tariffItemId = "new";
	}
	if(typeof useFilter == 'undefined')
	{
		useFilter = 0;
	}
	if(typeof showUsed == 'undefined')
	{
		showUsed = 0;
	}
	if(typeof selectedId == 'undefined')
	{
		selectedId = -1;
	}
	
	var select = $('select[name=tariffPlanId]:visible');
	
	var ajaxResponse = sendAJAXCommandWithParams( "plugin/bgbilling/proto/billingCrm.do?", { "action" : "getContractTariffPlanList", "billingId": billingId, 
		"contractId": contractId, "moduleId": moduleId, "tariffItemId": tariffItemId, "useFilter": useFilter, "showUsed": showUsed } );
	if( ajaxResponse )
	{
		select.children().remove();
		var tariffPlanList = ajaxResponse.data.tariffPlanList;
		for(var i=0; i<tariffPlanList.length ; i++)
		{
			if(tariffPlanList[i].id == selectedId)
			{
				select.append("<option value="+ tariffPlanList[i].id +" selected>" + tariffPlanList[i].title + "</option>");
			}
			else
			{
				select.append("<option value="+ tariffPlanList[i].id +">" + tariffPlanList[i].title + "</option>");
			}
		}
	}
}*/

/*function bgbilling_updateAvailableOptionList( billingId, contractId )
{
	var select = $('select[name=optionId]:visible');
	
	var ajaxResponse = sendAJAXCommandWithParams( "plugin/bgbilling/proto/billingCrm.do?", { "action" : "contractAvailableOptionList", "billingId": billingId, "contractId":contractId } );
	if( ajaxResponse )
	{
		select.children().remove();
		var availableOptionList = ajaxResponse.data.availableOptionList;
		for(var i=0; i<availableOptionList.length ; i++)
		{
			select.append("<option value="+ availableOptionList[i].id +">" + availableOptionList[i].title + "</option>");
		}
		if(availableOptionList.length == 0)
		{
			select.append("<option value=-1>Нет доступных опций</option>");
		}
		else
		{
			bgbilling_updateActivateModeList( billingId, availableOptionList[0].id )
		}
	}
}

function bgbilling_updateActivateModeList(billingId,optionId )
{
	var select = $('select[name=modeId]:visible');
	select.children().remove();
	if(optionId >= 0)
	{
		var ajaxResponse = sendAJAXCommandWithParams( "plugin/bgbilling/proto/billingCrm.do?", { "action" : "activateModeList","billingId": billingId, "optionId": optionId } );
		if( ajaxResponse )
		{
			var activateModeList = ajaxResponse.data.activateModeList;
			for(var i=0; i<activateModeList.length ; i++)
			{
				select.append("<option value="+ activateModeList[i].id +">" + activateModeList[i].title + "</option>");
			}
		}
	}
	else
	{
		select.append("<option value=-1>Нет доступных режимов</option>");
	}
	
}*/

function bgbilling_getLoginPassword(billingId,contractId,login,moduleId )
{
	if($('#'+login+'loginPassword:visible').size() == 0)
	{
		var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/bgbilling/proto/dialup.do?", { "action" : "getLoginPassword","billingId": billingId, "contractId": contractId, "login":login, "moduleId":moduleId } );
		if( ajaxResponse )
		{
			$('#'+login+'loginPassword').html('Пароль доступа:</br>'+ajaxResponse.data.password);
		}
	}
}

function bgbilling_getContractStatisticPassword(billingId,contractId )
{
	if($('#'+contractId+'statisticPassword:visible').size() == 0)
	{
		var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/bgbilling/proto/contract.do?", { "action" : "getContractStatisticPassword","billingId": billingId, "contractId": contractId } );
		if( ajaxResponse )
		{
			$('#'+contractId+'statisticPassword').html('Пароль доступа к статистике:</br>'+ajaxResponse.data.password);
		}
	}
}

function bgbilling_getSubContractList( billingId, contractId )
{
	var result = sendAJAXCommand( "/user/plugin/bgbilling/proto/contract.do?action=getSubContractList&billingId=" + billingId + "&contractId=" + contractId );
	return result.data.subContractList;
}

var cerbercrypt = {};

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
}

var voip = {};

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
}
