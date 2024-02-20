// "use strict";

$$.customer = new function() {
	/**
	 * Open customer tab.
	 * @param {*} id customer ID.
	 */
	const open = (id) => {
		return $$.shell.contentLoad("customer#" + id);
	}

	/**
	 * Create new customer and open it.
	 * @param {*} options.disabled UI element to be disabled.
	 */
	const createAndEdit = (options) => {
		options = options || {};

		if (options.disabled)
			options.disabled.disabled = true;

		let url = "/user/customer.do?action=customerCreate";
		$$.ajax
			.post(url).done((result) => {
				const customerId = result.data.customer.id;
				open(customerId).done(() => {
					url = "/user/customer.do?action=customerGet&id=" + customerId + "&returnUrl=" + encodeURIComponent("customer.do?id=" + customerId);
					$$.ajax.loadContent(url);
				})
			})
			.always(() => {
				if (options.disabled)
					options.disabled.disabled = false;
			})
	}

	// public functions
	this.open = open;
	this.createAndEdit = createAndEdit;
}

function openCustomer(id) {
	console.warn($$.deprecated);
	$$.customer.open(id);
}

function createCustomerAndEdit(sender) {
	console.warn($$.deprecated);
	$$.customer.createAndEdit(sender);
}

function buildOpenedCustomerList( $selector, currentCustomer )
{
	var $ul = $selector.find( 'ul.drop' );

	$ul.html( "" );

	var options;

	if( currentCustomer && currentCustomer.id > 0 )
	{
		options = "<li value='-1'>----</li>";
		/*options += "<li value='" + currentCustomer.id + "'>" + currentCustomer.title + "</li>";*/

		$selector.find( 'input[type=hidden]' ).attr( "value", currentCustomer.id );
		$selector.find( '.text-value' ).text( currentCustomer.title );
	}
	else
	{
		options = "";
		$selector.find( 'input[type=hidden]' ).attr( "value", 0 );
		/*options = "<li value='-1'>----</li>";*/
	}

	var openedCustomers = openedObjectList( { "typesInclude" : ["customer"] } );
	for( var c in openedCustomers )
	{
		if( currentCustomer == undefined || openedCustomers[c].id != currentCustomer.id )
		{
			options += "<li value='" + openedCustomers[c].id + "'>" + openedCustomers[c].title + "</li>";
		}
	}

	$ul.html( options );
}

// обработка клиентских событий
addEventProcessor( 'ru.bgcrm.event.client.CustomerTitleChangedEvent', processCustomerClientEvents );
addEventProcessor( 'ru.bgcrm.event.client.CustomerOpenEvent', processCustomerClientEvents );

function processCustomerClientEvents( event )
{
	if( event.className == 'ru.bgcrm.event.client.CustomerTitleChangedEvent' )
	{
		customerChangeTitle( event.id, event.title );
	}
	else if( event.className == 'ru.bgcrm.event.client.CustomerOpenEvent' )
	{
		openCustomer( event.id );
	}
}

// изменяет название контрагента на странице
function customerChangeTitle( customerId, customerTitle ) {
	$("#customer_title_" + customerId).text( customerTitle );
}

// запросы на сервер
function addCustomerLink( customerId, linkedObjectType, linkedObjectId, linkedObjectTitle )
{
	const url = "/user/link.do?action=addLink&id=" + customerId + '&' + $$.ajax.requestParamsToUrl({ "objectType": "customer", "linkedObjectType": linkedObjectType, "linkedObjectId": linkedObjectId, "linkedObjectTitle": linkedObjectTitle });
	return $$.ajax.post(url);
}

function deleteCustomerLinkTo( linkedObjectType, linkedObjectId )
{
	const url = "/user/link.do?action=deleteLinksTo&" + $$.ajax.requestParamsToUrl({ "objectType": "customer", "linkedObjectType": linkedObjectType, "linkedObjectId": linkedObjectId });
	return $$.ajax.post(url);
}
