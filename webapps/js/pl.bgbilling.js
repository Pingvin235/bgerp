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

		/**
		 * Opens contract in tab when checkbox is clicked.
		 * @param {*} searchTabsId
		 * @param {*} scriptId
		 * @param {*} linkedObjectTitle
		 * @param {*} billingId
		 * @param {*} contractId
		 */
		const onCheckTabOpen = (searchTabsId, scriptId, linkedObjectTitle, billingId, contractId) => {
			document
				.getElementById(scriptId).closest('tr').querySelector('input[type=checkbox]')
				.addEventListener('change', (event) => {
					if (event.currentTarget.checked) {
						const url = '/user/plugin/bgbilling/contract.do?billingId='+ billingId + '&id=' + contractId + '&inBuffer=0';
						const tabId = billingId + '-' + contractId;
						if (!document.getElementById(searchTabsId).querySelector('#' + tabId))
							$('#' + searchTabsId).tabs().tabs('add', url, linkedObjectTitle, ' id='+ tabId);
					}
				});
		}

		// public functions
		this.open = open;
		this.createTariff = createTariff;
		this.create = create;
		this.onCheckTabOpen = onCheckTabOpen;
	}

	const processContractOpenEvent = (event) => {
		contract.open(event.billingId, event.contractId);
	}
	addEventProcessor('ru.bgcrm.plugin.bgbilling.event.client.ContractOpenEvent', processContractOpenEvent);

	// public objects
	this.contract = contract;
}

// загрузка шаблонов договоров в форму создания
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

/* Remove later.
function bgbilling_openContract( billingId, contractId ) {
	console.warn($$.deprecated);
	$$.bgbilling.contract.open(billingId, contractId);
}
*/